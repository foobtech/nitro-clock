package dev.nitrodroid.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val kind = i.getStringExtra("kind") ?: return
        var title = i.getStringExtra("title") ?: "Alarm"
        when (kind) {
            "alarm" -> {
                val id = i.getIntExtra("id", 0)
                val list = AlarmStore.load(c)
                val a = list.find { it.id == id } ?: return
                title = a.label.ifBlank { "Alarm" }
                if (a.days == 0) {
                    // one-time alarm: switch it off after it rings
                    AlarmStore.save(c, list.map { if (it.id == id) it.copy(enabled = false) else it })
                } else {
                    Scheduler.schedule(c, a) // repeating: queue the next occurrence
                }
            }
            "timer" -> title = "Timer finished"
        }
        ContextCompat.startForegroundService(c, Intent(c, RingService::class.java).putExtra("title", title))
    }
}

/** Re-arms everything after reboot, time or timezone changes, and app updates. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        AlarmStore.load(c).filter { it.enabled }.forEach { Scheduler.schedule(c, it) }
        val end = AlarmStore.timerEnd(c)
        if (end > System.currentTimeMillis()) Scheduler.scheduleTimer(c, end)
    }
}
