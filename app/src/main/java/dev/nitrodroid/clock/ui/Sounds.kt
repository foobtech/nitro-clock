package dev.nitrodroid.clock.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.IntentCompat

/** Human-readable name for a stored sound ("" means the system default alarm tone). */
fun soundName(c: Context, s: String): String = try {
    val uri = if (s.isBlank()) android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI else Uri.parse(s)
    RingtoneManager.getRingtone(c, uri)?.getTitle(c) ?: "Default"
} catch (e: Exception) {
    "Default"
}

/** Returns a function that opens the system sound picker; onPicked gets the chosen URI as a string. */
@Composable
fun rememberSoundPicker(current: String, onPicked: (String) -> Unit): () -> Unit {
    val ctx = LocalContext.current
    val askAudio = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.let {
                IntentCompat.getParcelableExtra(it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            }
            onPicked(uri?.toString() ?: "")
            // Songs from storage need audio access to be played by the alarm service.
            if (uri != null && uri.toString().startsWith("content://media/external")) {
                val perm = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE
                if (ctx.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) askAudio.launch(perm)
            }
        }
    }
    return {
        val default = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
        val existing = if (current.isBlank()) default else Uri.parse(current)
        launcher.launch(
            Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, default)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existing)
        )
    }
}
