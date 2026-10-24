package dev.nitrodroid.clock.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class StopwatchState {
    var running by mutableStateOf(false)
        private set
    var elapsedMs by mutableLongStateOf(0L)
        private set
    val laps = mutableStateListOf<Long>()
    private var startedAt = 0L
    private var base = 0L

    fun toggle() {
        if (running) {
            base += SystemClock.elapsedRealtime() - startedAt
            elapsedMs = base
            running = false
        } else {
            startedAt = SystemClock.elapsedRealtime()
            running = true
        }
    }

    fun tick() { if (running) elapsedMs = base + SystemClock.elapsedRealtime() - startedAt }
    fun lap() { if (running) laps.add(0, elapsedMs) }
    fun reset() { running = false; base = 0L; elapsedMs = 0L; laps.clear() }
}

@Composable
fun StopwatchScreen(sw: StopwatchState) {
    LaunchedEffect(sw.running) {
        while (sw.running) { sw.tick(); delay(30) }
    }
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(48.dp))
        Text(formatStopwatch(sw.elapsedMs), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(onClick = sw::reset, enabled = sw.elapsedMs > 0L) { Text("Reset") }
            LargeFloatingActionButton(onClick = sw::toggle) {
                Icon(
                    if (sw.running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (sw.running) "Pause" else "Start",
                )
            }
            FilledTonalButton(onClick = sw::lap, enabled = sw.running) { Text("Lap") }
        }
        LazyColumn(Modifier.fillMaxWidth().padding(top = 24.dp)) {
            itemsIndexed(sw.laps) { i, ms ->
                ListItem(
                    headlineContent = { Text("Lap ${sw.laps.size - i}") },
                    trailingContent = { Text(formatStopwatch(ms)) },
                )
            }
        }
    }
}
