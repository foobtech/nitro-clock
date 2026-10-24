@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.nitrodroid.clock.ui

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class Lap(val n: Int, val lap: Long, val total: Long)

class StopwatchState {
    var running by mutableStateOf(false)
        private set
    var elapsedMs by mutableLongStateOf(0L)
        private set
    val laps = mutableStateListOf<Lap>()
    private var startedAt = 0L
    private var base = 0L
    private var lastLapAt = 0L

    private fun current(): Long = if (running) base + SystemClock.elapsedRealtime() - startedAt else base

    fun toggle() {
        if (running) {
            base = current()
            elapsedMs = base
            running = false
        } else {
            startedAt = SystemClock.elapsedRealtime()
            running = true
        }
    }

    fun tick() { if (running) elapsedMs = current() }

    fun lap() {
        if (!running) return
        val t = current()
        laps.add(Lap(laps.size + 1, t - lastLapAt, t))
        lastLapAt = t
    }

    fun reset() {
        running = false
        base = 0L
        elapsedMs = 0L
        lastLapAt = 0L
        laps.clear()
    }
}

@Composable
fun StopwatchScreen(sw: StopwatchState) {
    val cs = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    LaunchedEffect(sw.running) {
        while (sw.running) { sw.tick(); delay(30) }
    }
    LaunchedEffect(sw.laps.size) {
        if (sw.laps.isNotEmpty()) listState.animateScrollToItem(sw.laps.size - 1)
    }
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(48.dp))
        Text(
            formatStopwatch(sw.elapsedMs),
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 64.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
        Spacer(Modifier.height(24.dp))
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(sw.laps) { lap ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, cs.outlineVariant),
                ) {
                    Column(
                        Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("%02d".format(lap.n), style = MaterialTheme.typography.titleMedium)
                        Text(formatStopwatch(lap.lap), style = MaterialTheme.typography.titleMedium)
                        Text(
                            formatStopwatch(lap.total),
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = sw::toggle,
                modifier = Modifier.fillMaxWidth().height(96.dp),
                shape = RoundedCornerShape(48.dp),
                colors = if (sw.running) ButtonDefaults.buttonColors(
                    containerColor = cs.errorContainer, contentColor = cs.onErrorContainer,
                ) else ButtonDefaults.buttonColors(
                    containerColor = cs.primaryContainer, contentColor = cs.onPrimaryContainer,
                ),
            ) { Text(if (sw.running) "Stop" else "Start", fontSize = 28.sp) }
            if (sw.elapsedMs > 0L) {
                FilledTonalButton(
                    onClick = sw::reset,
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    shape = RoundedCornerShape(36.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = cs.surfaceContainerHigh),
                ) { Text("Reset", fontSize = 24.sp) }
            }
            if (sw.running) {
                FilledTonalButton(
                    onClick = sw::lap,
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    shape = RoundedCornerShape(36.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = cs.surfaceContainerHigh),
                ) { Text("Lap", fontSize = 24.sp) }
            }
        }
    }
}
