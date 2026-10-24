@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.nitrodroid.clock.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.nitrodroid.clock.AlarmStore
import dev.nitrodroid.clock.Prefs
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

fun cityName(zoneId: String) = zoneId.substringAfterLast('/').replace('_', ' ')

@Composable
fun WorldClockScreen() {
    val ctx = LocalContext.current
    val is24 = DateFormat.is24HourFormat(ctx)
    val seconds = Prefs.showSeconds(ctx)
    var zones by remember { mutableStateOf(AlarmStore.zones(ctx)) }
    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    var adding by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) { now = ZonedDateTime.now(); delay(500) }
    }

    fun setZones(list: List<String>) { zones = list; AlarmStore.saveZones(ctx, list) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 128.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val (main, suffix) = clockParts(now, is24, seconds)
                    TimeText(main, suffix, 80.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        now.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            if (zones.isEmpty()) {
                item {
                    Text(
                        "No locations",
                        Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            items(zones, key = { it }) { id -> ZoneCard(id, now, is24, onRemove = { setZones(zones - id) }) }
        }
        LargeFloatingActionButton(
            onClick = { adding = true },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        ) { Icon(Icons.Rounded.Add, contentDescription = "Add city", modifier = Modifier.size(36.dp)) }
    }

    if (adding) {
        CityPicker(
            onDismiss = { adding = false },
            onPick = { id -> if (id !in zones) setZones(zones + id); adding = false },
        )
    }
}

@Composable
private fun ZoneCard(id: String, now: ZonedDateTime, is24: Boolean, onRemove: () -> Unit) {
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
        sign + (abs(diffMin) / 60) + "h" + (if (mins != 0) " " + mins + "m" else "")
    }
    val (main, suffix) = clockParts(t, is24, false)
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(cityName(id), style = MaterialTheme.typography.titleLarge)
                Text(
                    day + " · " + diff,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TimeText(main, suffix, 40.sp)
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, contentDescription = "Remove " + cityName(id))
            }
        }
    }
}

@Composable
private fun CityPicker(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val all = remember {
        ZoneId.getAvailableZoneIds()
            .filter { '/' in it && !it.startsWith("Etc/") && !it.startsWith("SystemV/") }
            .sorted()
    }
    val shown = remember(query) {
        if (query.isBlank()) emptyList()
        else all.filter { it.replace('_', ' ').contains(query.trim(), ignoreCase = true) }
    }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search for a city") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier.weight(1f).focusRequester(focus),
                    )
                }
                HorizontalDivider()
                if (shown.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Search, contentDescription = null,
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (query.isBlank()) "Search for a city" else "No matches",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(shown, key = { it }) { id ->
                            ListItem(
                                headlineContent = { Text(cityName(id)) },
                                supportingContent = { Text(id) },
                                modifier = Modifier.clickable { onPick(id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
