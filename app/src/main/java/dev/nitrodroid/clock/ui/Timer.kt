package dev.nitrodroid.clock.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.nitrodroid.clock.AlarmStore
import dev.nitrodroid.clock.RingService
import dev.nitrodroid.clock.Scheduler
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerScreen() {
    val ctx = LocalContext.current
    var end by remember { mutableLongStateOf(AlarmStore.timerEnd(ctx)) }
    var total by remember { mutableLongStateOf(AlarmStore.timerTotal(ctx)) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var custom by remember { mutableStateOf("") }

    LaunchedEffect(end) {
        while (end != 0L) {
            now = System.currentTimeMillis()
            if (now >= end) break
            delay(100)
        }
    }

    fun start(ms: Long) {
        total = ms
        end = System.currentTimeMillis() + ms
        now = System.currentTimeMillis()
        AlarmStore.setTimer(ctx, end, total)
        Scheduler.scheduleTimer(ctx, end)
    }

    fun clear() {
        end = 0L
        total = 0L
        AlarmStore.setTimer(ctx, 0L, 0L)
        Scheduler.cancelTimer(ctx)
        // silence the ringing service if the timer already went off
        ctx.startService(Intent(ctx, RingService::class.java).setAction(RingService.ACTION_DISMISS))
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (end == 0L) {
            Text("Start a timer", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                listOf(1, 3, 5, 10, 15, 30).forEach { m ->
                    FilledTonalButton(onClick = { start(m * 60_000L) }) { Text("$m min") }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it.filter(Char::isDigit).take(4) },
                    label = { Text("Custom minutes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(180.dp),
                )
                Button(
                    enabled = (custom.toIntOrNull() ?: 0) > 0,
                    onClick = { start(custom.toLong() * 60_000L); custom = "" },
                ) { Text("Start") }
            }
        } else {
            val remaining = (end - now).coerceAtLeast(0L)
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (total > 0) remaining.toFloat() / total else 0f },
                    modifier = Modifier.size(280.dp),
                    strokeWidth = 14.dp,
                )
                Text(
                    if (remaining == 0L) "Time's up" else formatCountdown(remaining),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (remaining > 0L) {
                    FilledTonalButton(onClick = {
                        end += 60_000L
                        total += 60_000L
                        AlarmStore.setTimer(ctx, end, total)
                        Scheduler.scheduleTimer(ctx, end)
                    }) { Text("Add 1 min") }
                }
                Button(onClick = { clear() }) { Text(if (remaining == 0L) "Done" else "Cancel") }
            }
        }
    }
}
