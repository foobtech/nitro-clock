@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.nitrodroid.clock.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nitrodroid.clock.AlarmStore
import dev.nitrodroid.clock.RingService
import dev.nitrodroid.clock.Scheduler
import kotlinx.coroutines.delay

@Composable
fun TimerScreen() {
    val ctx = LocalContext.current
    var end by remember { mutableLongStateOf(AlarmStore.timerEnd(ctx)) }
    var total by remember { mutableLongStateOf(AlarmStore.timerTotal(ctx)) }
    var paused by remember { mutableLongStateOf(AlarmStore.timerPaused(ctx)) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var digits by remember { mutableStateOf("") }

    LaunchedEffect(end) {
        while (end != 0L) {
            now = System.currentTimeMillis()
            if (now >= end) break
            delay(100)
        }
    }

    fun save() = AlarmStore.setTimer(ctx, end, total, paused)

    fun start(ms: Long) {
        total = ms
        now = System.currentTimeMillis()
        end = now + ms
        paused = 0L
        save()
        Scheduler.scheduleTimer(ctx, end)
    }

    fun pause() {
        paused = (end - System.currentTimeMillis()).coerceAtLeast(1000L)
        end = 0L
        save()
        Scheduler.cancelTimer(ctx)
    }

    fun resume() {
        now = System.currentTimeMillis()
        end = now + paused
        paused = 0L
        save()
        Scheduler.scheduleTimer(ctx, end)
    }

    fun addMinute() {
        total += 60_000L
        if (paused > 0L) {
            paused += 60_000L
        } else {
            end += 60_000L
            Scheduler.scheduleTimer(ctx, end)
        }
        save()
    }

    fun clear() {
        end = 0L
        total = 0L
        paused = 0L
        save()
        Scheduler.cancelTimer(ctx)
        // silence the ringing service if the timer already went off
        ctx.startService(Intent(ctx, RingService::class.java).setAction(RingService.ACTION_DISMISS))
    }

    if (end == 0L && paused == 0L) {
        Keypad(
            digits = digits,
            onKey = { k ->
                digits = when {
                    k == "<" -> digits.dropLast(1)
                    digits.length + k.length > 6 -> digits
                    digits.isEmpty() && k.all { it == '0' } -> digits
                    else -> digits + k
                }
            },
            onStart = { ms -> start(ms); digits = "" },
        )
    } else {
        val remaining = if (paused > 0L) paused else (end - now).coerceAtLeast(0L)
        val finished = paused == 0L && remaining == 0L
        val cs = MaterialTheme.colorScheme
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            timerLabel(total),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        FilledTonalIconButton(onClick = { clear() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Delete timer")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { if (total > 0L) remaining.toFloat() / total else 0f },
                            modifier = Modifier.size(280.dp),
                            strokeWidth = 14.dp,
                            trackColor = cs.surfaceContainerHigh,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            if (finished) "0" else formatRing(remaining),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Light,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    if (finished) {
                        Button(
                            onClick = { clear() },
                            modifier = Modifier.fillMaxWidth().height(72.dp),
                            shape = RoundedCornerShape(36.dp),
                        ) { Text("Done", fontSize = 20.sp) }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalButton(
                                onClick = { addMinute() },
                                modifier = Modifier.weight(1f).height(72.dp),
                                shape = RoundedCornerShape(36.dp),
                            ) { Text("+1:00", fontSize = 20.sp) }
                            Button(
                                onClick = { if (paused > 0L) resume() else pause() },
                                modifier = Modifier.weight(1f).height(72.dp),
                                shape = RoundedCornerShape(36.dp),
                            ) {
                                Icon(
                                    if (paused > 0L) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = if (paused > 0L) "Resume" else "Pause",
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Keypad(digits: String, onKey: (String) -> Unit, onStart: (Long) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val padded = digits.padStart(6, '0')
    val h = padded.substring(0, 2)
    val m = padded.substring(2, 4)
    val s = padded.substring(4, 6)
    val totalSeconds = h.toLong() * 3600 + m.toLong() * 60 + s.toLong()
    val digitColor = if (digits.isEmpty()) cs.onSurfaceVariant else cs.onSurface

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            listOf(h to "h", m to "m", s to "s").forEach { (value, unit) ->
                Text(value, fontSize = 64.sp, fontWeight = FontWeight.Light, color = digitColor)
                Text(
                    unit,
                    fontSize = 22.sp,
                    color = digitColor,
                    modifier = Modifier.padding(start = 2.dp, end = 12.dp, bottom = 10.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("00", "0", "<"),
        ).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { k -> KeyButton(if (k == "<") "\u232B" else k) { onKey(k) } }
            }
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(8.dp))
        val canStart = totalSeconds > 0L
        Surface(
            onClick = { onStart(totalSeconds * 1000L) },
            enabled = canStart,
            shape = CircleShape,
            color = if (canStart) cs.primaryContainer else cs.surfaceContainerHigh,
            modifier = Modifier.size(84.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Start timer", modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun KeyButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(84.dp),
    ) {
        Box(contentAlignment = Alignment.Center) { Text(label, fontSize = 30.sp) }
    }
}
