package dev.nitrodroid.clock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessAlarm
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

@Composable
fun NitroClockApp() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val stopwatch = remember { StopwatchState() }
    val tabs = listOf(
        "Alarm" to Icons.Rounded.AccessAlarm,
        "Clock" to Icons.Rounded.Public,
        "Timer" to Icons.Rounded.HourglassEmpty,
        "Stopwatch" to Icons.Rounded.Timer,
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(label) },
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                0 -> AlarmsScreen()
                1 -> WorldClockScreen()
                2 -> TimerScreen()
                else -> StopwatchScreen(stopwatch)
            }
        }
    }
}
