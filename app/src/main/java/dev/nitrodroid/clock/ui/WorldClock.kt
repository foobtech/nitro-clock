package dev.nitrodroid.clock.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nitrodroid.clock.AlarmStore
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import kotlin.math.abs

fun cityName(zoneId: String) = zoneId.substringAfterLast('/').replace('_', ' ')

@Composable
fun WorldClockScreen() {
    val ctx = LocalContext.current
    var zones by remember { mutableStateOf(AlarmStore.zones(ctx)) }
    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    var adding by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) { now = ZonedDateTime.now(); delay(1000) }
    }

    fun setZones(list: List<String>) { zones = list; AlarmStore.saveZones(ctx, list) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(vertical = 16.dp)) {
                    Text(
                        now.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        now.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)) + " · " + cityName(now.zone.id),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            items(zones, key = { it }) { id -> ZoneCard(id, now, onRemove = { setZones(zones - id) }) }
        }
        ExtendedFloatingActionButton(
            onClick = { adding = true },
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text("Add city") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (adding) {
        ZonePicker(
            onDismiss = { adding = false },
            onPick = { id -> if (id !in zones) setZones(zones + id); adding = false },
        )
    }
}

@Composable
private fun ZoneCard(id: String, now: ZonedDateTime, onRemove: () -> Unit) {
    val t = now.withZoneSameInstant(ZoneId.of(id))
    val dayDiff = ChronoUnit.DAYS.between(now.toLocalDate(), t.toLocalDate())
    val day = when (dayDiff) {
        0L -> "Today"
        1L -> "Tomorrow"
        -1L -> "Yesterday"
        else -> t.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    val diffMin = (t.offset.totalSeconds - now.offset.totalSeconds) / 60
    val diff = if (diffMin == 0) "Same time as you" else {
        val sign = if (diffMin > 0) "+" else "-"
        val mins = abs(diffMin) % 60
        "$sign${abs(diffMin) / 60}h" + if (mins != 0) " ${mins}m" else ""
    }
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(Modifier.padding(start = 24.dp, top = 16.dp, bottom = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(cityName(id), style = MaterialTheme.typography.titleLarge)
                Text("$day · $diff", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                t.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onRemove) { Icon(Icons.Rounded.Close, contentDescription = "Remove ${cityName(id)}") }
        }
    }
}

@Composable
private fun ZonePicker(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val all = remember {
        ZoneId.getAvailableZoneIds()
            .filter { '/' in it && !it.startsWith("Etc/") && !it.startsWith("SystemV/") }
            .sorted()
    }
    val shown = remember(query) { all.filter { it.replace('_', ' ').contains(query.trim(), ignoreCase = true) } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add city") },
        text = {
            Column {
                OutlinedTextField(query, { query = it }, singleLine = true, label = { Text("Search, for example Tokyo") })
                LazyColumn(Modifier.height(320.dp)) {
                    items(shown, key = { it }) { id ->
                        ListItem(
                            headlineContent = { Text(cityName(id)) },
                            supportingContent = { Text(id) },
                            modifier = Modifier.clickable { onPick(id) },
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
