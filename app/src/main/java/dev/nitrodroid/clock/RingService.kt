package dev.nitrodroid.clock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.flow.MutableStateFlow

class RingService : Service() {
    companion object {
        const val ACTION_DISMISS = "dev.nitrodroid.clock.DISMISS"
        const val ACTION_SNOOZE = "dev.nitrodroid.clock.SNOOZE"
        const val SNOOZE_MINUTES = 10
        private const val CHANNEL = "ring"
        val ringing = MutableStateFlow(false)
    }

    private var ringtone: Ringtone? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra("title") ?: "Alarm"
        when (intent?.action) {
            ACTION_SNOOZE -> { Scheduler.snooze(this, SNOOZE_MINUTES, title); finishRinging(); return START_NOT_STICKY }
            ACTION_DISMISS -> { finishRinging(); return START_NOT_STICKY }
        }

        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Alarms and timers", NotificationManager.IMPORTANCE_HIGH)
                .apply { setSound(null, null) }
        )
        val flagsPi = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val open = PendingIntent.getActivity(this, 0, Intent(this, RingActivity::class.java).putExtra("title", title), flagsPi)
        val dismiss = PendingIntent.getService(this, 1, Intent(this, RingService::class.java).setAction(ACTION_DISMISS), flagsPi)
        val snooze = PendingIntent.getService(
            this, 2,
            Intent(this, RingService::class.java).setAction(ACTION_SNOOZE).putExtra("title", title), flagsPi
        )
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_nitro)
            .setContentTitle(title)
            .setContentText("Tap to open")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setContentIntent(open)
            .setFullScreenIntent(open, true)
            .addAction(0, "Snooze", snooze)
            .addAction(0, "Dismiss", dismiss)
            .build()
        ServiceCompat.startForeground(this, 1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)

        ringing.value = true
        startRinging()
        return START_NOT_STICKY
    }

    private fun startRinging() {
        stopRinging()
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone = RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))?.apply {
            audioAttributes = attrs
            isLooping = true
            play()
        }
        @Suppress("DEPRECATION")
        getSystemService(Vibrator::class.java)
            ?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 700, 500), 0), attrs)
    }

    private fun stopRinging() {
        ringtone?.stop()
        ringtone = null
        getSystemService(Vibrator::class.java)?.cancel()
    }

    private fun finishRinging() {
        stopRinging()
        ringing.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopRinging()
        ringing.value = false
        super.onDestroy()
    }
}
