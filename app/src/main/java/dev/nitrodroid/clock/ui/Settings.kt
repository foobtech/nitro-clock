@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.nitrodroid.clock.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.nitrodroid.clock.Prefs

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    var alarmSound by remember { mutableStateOf(Prefs.alarmSound(ctx)) }
    var timerSound by remember { mutableStateOf(Prefs.timerSound(ctx)) }
    var snooze by remember { mutableIntStateOf(Prefs.snoozeMin(ctx)) }
    var alarmVibrate by remember { mutableStateOf(Prefs.alarmVibrate(ctx)) }
    var timerVibrate by remember { mutableStateOf(Prefs.timerVibrate(ctx)) }
    var seconds by remember { mutableStateOf(Prefs.showSeconds(ctx)) }
    var snoozeDialog by remember { mutableStateOf(false) }

    val pickAlarm = rememberSoundPicker(alarmSound) { alarmSound = it; Prefs.setAlarmSound(ctx, it) }
    val pickTimer = rememberSoundPicker(timerSound) { timerSound = it; Prefs.setTimerSound(ctx, it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())) {
            SectionTitle("Alarms")
            ClickRow("Alarm sound", soundName(ctx, alarmSound), pickAlarm)
            ClickRow("Snooze length", "$snooze minutes") { snoozeDialog = true }
            SwitchRow("Vibrate", "Vibrate when an alarm rings", alarmVibrate) {
                alarmVibrate = it; Prefs.setAlarmVibrate(ctx, it)
            }

            SectionTitle("Timers")
            ClickRow("Timer sound", soundName(ctx, timerSound), pickTimer)
            SwitchRow("Vibrate", "Vibrate when a timer ends", timerVibrate) {
                timerVibrate = it; Prefs.setTimerVibrate(ctx, it)
            }

            SectionTitle("World clock")
            SwitchRow("Display time with seconds", "Shown on the World clock tab", seconds) {
                seconds = it; Prefs.setShowSeconds(ctx, it)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SectionTitle("Appearance")
                SwitchRow("Use wallpaper colors", "Material You instead of Nitro blue", ThemeState.dynamic) {
                    ThemeState.dynamic = it; Prefs.setDynamicColor(ctx, it)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (snoozeDialog) {
        AlertDialog(
            onDismissRequest = { snoozeDialog = false },
            title = { Text("Snooze length") },
            text = {
                Column {
                    listOf(5, 10, 15, 20, 30).forEach { m ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { snooze = m; Prefs.setSnoozeMin(ctx, m); snoozeDialog = false }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = snooze == m, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text("$m minutes")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { snoozeDialog = false }) { Text("Close") } },
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 4.dp),
    )
}

@Composable
private fun ClickRow(title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() },
    )
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onChange(!checked) },
    )
}
