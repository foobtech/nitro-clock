package dev.nitrodroid.clock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nitrodroid.clock.ui.NitroClockTheme
import dev.nitrodroid.clock.ui.formatTime
import java.time.LocalTime

class RingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val title = intent.getStringExtra("title") ?: "Alarm"
        setContent {
            NitroClockTheme {
                val ringing by RingService.ringing.collectAsState()
                LaunchedEffect(ringing) { if (!ringing) finish() }
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val now = LocalTime.now()
                        Text(formatTime(now.hour, now.minute), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(48.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            FilledTonalButton(onClick = { send(RingService.ACTION_SNOOZE, title) }) {
                                Text("Snooze ${RingService.SNOOZE_MINUTES} min")
                            }
                            Button(onClick = { send(RingService.ACTION_DISMISS, title) }) { Text("Dismiss") }
                        }
                    }
                }
            }
        }
    }

    private fun send(action: String, title: String) {
        startService(Intent(this, RingService::class.java).setAction(action).putExtra("title", title))
        finish()
    }
}
