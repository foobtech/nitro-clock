package dev.nitrodroid.clock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Uses AlarmManager.setAlarmClock, which needs no special permission, fires exactly,
 * and may start the ringing service from the background.
 */
object Scheduler {
    private const val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    private const val TIMER_CODE = 0
    private const val SNOOZE_CODE = 100000

    private fun fire(
        c: Context, code: Int, kind: String, id: Int = 0,
        title: String? = null, sound: String = "", vibrate: Boolean = true,
    ): PendingIntent {
        val i = Intent(c, AlarmReceiver::class.java)
            .putExtra("kind", kind).putExtra("id", id)
            .putExtra("sound", sound).putExtra("vibrate", vibrate)
        if (title != null) i.putExtra("title", title)
        return PendingIntent.getBroadcast(c, code, i, FLAGS)
    }

    private fun setClock(c: Context, atMs: Long, op: PendingIntent) {
        val show = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        c.getSystemService(AlarmManager::class.java)
            .setAlarmClock(AlarmManager.AlarmClockInfo(atMs, show), op)
    }

    fun schedule(c: Context, a: Alarm) {
        if (!a.enabled) { cancel(c, a.id); return }
        setClock(c, a.nextTrigger().toInstant().toEpochMilli(), fire(c, a.id, "alarm", a.id))
    }

    fun cancel(c: Context, id: Int) =
        c.getSystemService(AlarmManager::class.java).cancel(fire(c, id, "alarm", id))

    fun scheduleTimer(c: Context, endMs: Long) = setClock(c, endMs, fire(c, TIMER_CODE, "timer"))

    fun cancelTimer(c: Context) =
        c.getSystemService(AlarmManager::class.java).cancel(fire(c, TIMER_CODE, "timer"))

    fun snooze(c: Context, minutes: Int, title: String, sound: String, vibrate: Boolean) =
        setClock(
            c, System.currentTimeMillis() + minutes * 60_000L,
            fire(c, SNOOZE_CODE, "snooze", 0, title, sound, vibrate),
        )
}
