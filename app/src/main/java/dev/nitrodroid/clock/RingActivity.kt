package dev.nitrodroid.clock

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nitrodroid.clock.ui.NitroClockTheme
import dev.nitrodroid.clock.ui.ThemeState
import dev.nitrodroid.clock.ui.TimeText
import dev.nitrodroid.clock.ui.clockParts
import java.time.ZonedDateTime

class RingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        ThemeState.init(this)
        val title = intent.getStringExtra("title") ?: "Alarm"
        val sound = intent.getStringExtra("sound") ?: ""
        val vibrate = intent.getBooleanExtra("vibrate", true)
        val snoozeMin = Prefs.snoozeMin(this)
        val is24 = DateFormat.is24HourFormat(this)
        setContent {
            NitroClockTheme {
                val ringing by RingService.ringing.collectAsState()
                LaunchedEffect(ringing) { if (!ringing) finish() }
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.weight(1f))
                        val (main, suffix) = clockParts(ZonedDateTime.now(), is24, false)
                        TimeText(main, suffix, 96.sp)
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.weight(1f))
                        FilledTonalButton(
                            onClick = { send(RingService.ACTION_SNOOZE, title, sound, vibrate) },
                            modifier = Modifier.fillMaxWidth().height(88.dp),
                            shape = RoundedCornerShape(44.dp),
                        ) { Text("Snooze $snoozeMin min", fontSize = 22.sp) }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { send(RingService.ACTION_DISMISS, title, sound, vibrate) },
                            modifier = Modifier.fillMaxWidth().height(88.dp),
                            shape = RoundedCornerShape(44.dp),
                        ) { Text("Dismiss", fontSize = 22.sp) }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    private fun send(action: String, title: String, sound: String, vibrate: Boolean) {
        startService(
            Intent(this, RingService::class.java).setAction(action)
                .putExtra("title", title).putExtra("sound", sound).putExtra("vibrate", vibrate)
        )
        finish()
    }
}
