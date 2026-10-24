@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.nitrodroid.clock.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nitrodroid.clock.Alarm
import dev.nitrodroid.clock.AlarmStore
import dev.nitrodroid.clock.Prefs
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
                "No alarms",
                Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 128.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
        LargeFloatingActionButton(
            onClick = { val t = LocalTime.now(); editing = Alarm(0, t.hour, t.minute) },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        ) { Icon(Icons.Rounded.Add, contentDescription = "New alarm", modifier = Modifier.size(36.dp)) }
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
    val cs = MaterialTheme.colorScheme
    val (main, suffix) = timeParts(a.hour, a.minute, DateFormat.is24HourFormat(LocalContext.current))
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    listOf(a.label, describeDays(a.days)).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurfaceVariant,
                )
                TimeText(main, suffix, 56.sp, color = if (a.enabled) cs.onSurface else cs.onSurfaceVariant)
            }
            Switch(checked = a.enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun AlarmEditor(a: Alarm, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (Alarm) -> Unit) {
    val ctx = LocalContext.current
    val picker = rememberTimePickerState(a.hour, a.minute, DateFormat.is24HourFormat(ctx))
    var label by remember { mutableStateOf(a.label) }
    var days by remember { mutableIntStateOf(a.days) }
    var sound by remember { mutableStateOf(a.sound) }
    val pickSound = rememberSoundPicker(sound.ifBlank { Prefs.alarmSound(ctx) }) { sound = it }

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
                FilledTonalButton(onClick = pickSound) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        soundName(ctx, sound.ifBlank { Prefs.alarmSound(ctx) }),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(a.copy(hour = picker.hour, minute = picker.minute, label = label.trim(), days = days, sound = sound))
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
