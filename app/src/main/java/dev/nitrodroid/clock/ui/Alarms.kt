package dev.nitrodroid.clock.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nitrodroid.clock.Alarm
import dev.nitrodroid.clock.AlarmStore
import dev.nitrodroid.clock.Scheduler
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AlarmsScreen() {
    val ctx = LocalContext.current
    var alarms by remember { mutableStateOf(AlarmStore.load(ctx)) }
    var editing by remember { mutableStateOf<Alarm?>(null) }

    fun commit(list: List<Alarm>) {
        alarms = list.sortedWith(compareBy({ it.hour }, { it.minute }))
        AlarmStore.save(ctx, alarms)
    }

    Box(Modifier.fillMaxSize()) {
        if (alarms.isEmpty()) {
            Text(
                "No alarms yet. Tap New alarm to add one.",
                Modifier.align(Alignment.Center).padding(32.dp),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(alarms, key = { it.id }) { a ->
                AlarmCard(
                    a,
                    onToggle = { on ->
                        val updated = a.copy(enabled = on)
                        Scheduler.schedule(ctx, updated)
                        commit(alarms.map { if (it.id == a.id) updated else it })
                    },
                    onClick = { editing = a },
                )
            }
        }
        ExtendedFloatingActionButton(
            onClick = { val t = LocalTime.now(); editing = Alarm(0, t.hour, t.minute) },
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text("New alarm") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    editing?.let { a ->
        AlarmEditor(
            a,
            onDismiss = { editing = null },
            onDelete = {
                if (a.id != 0) {
                    Scheduler.cancel(ctx, a.id)
                    commit(alarms.filter { it.id != a.id })
                }
                editing = null
            },
            onSave = { edited ->
                val saved = if (edited.id == 0) edited.copy(id = AlarmStore.nextId(ctx), enabled = true) else edited
                Scheduler.schedule(ctx, saved)
                commit(alarms.filter { it.id != saved.id } + saved)
                editing = null
            },
        )
    }
}

@Composable
private fun AlarmCard(a: Alarm, onToggle: (Boolean) -> Unit, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = if (a.enabled) colors.primaryContainer else colors.surfaceContainerHigh),
    ) {
        Row(Modifier.padding(horizontal = 24.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(formatTime(a.hour, a.minute), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text(
                    listOf(a.label.ifBlank { "Alarm" }, describeDays(a.days)).joinToString(" · "),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Switch(checked = a.enabled, onCheckedChange = onToggle)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlarmEditor(a: Alarm, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (Alarm) -> Unit) {
    val picker = rememberTimePickerState(a.hour, a.minute, DateFormat.is24HourFormat(LocalContext.current))
    var label by remember { mutableStateOf(a.label) }
    var days by remember { mutableIntStateOf(a.days) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (a.id == 0) "New alarm" else "Edit alarm") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TimePicker(state = picker)
                OutlinedTextField(label, { label = it }, label = { Text("Label") }, singleLine = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0..6) {
                        FilterChip(
                            selected = ((days shr i) and 1) == 1,
                            onClick = { days = days xor (1 shl i) },
                            label = { Text(DayOfWeek.of(i + 1).getDisplayName(TextStyle.NARROW, Locale.getDefault())) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(a.copy(hour = picker.hour, minute = picker.minute, label = label.trim(), days = days))
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (a.id != 0) TextButton(onClick = onDelete) { Text("Delete") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
