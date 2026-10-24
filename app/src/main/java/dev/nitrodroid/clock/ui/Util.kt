@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package dev.nitrodroid.clock.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DayTextStyle
import java.util.Locale

/** Big light-weight time with a small am/pm next to it. */
@Composable
fun TimeText(main: String, suffix: String, mainSize: TextUnit, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Text(
        buildAnnotatedString {
            append(main)
            if (suffix.isNotEmpty()) withStyle(SpanStyle(fontSize = mainSize * 0.33f)) { append(" " + suffix) }
        },
        modifier = modifier,
        color = color,
        fontSize = mainSize,
        fontWeight = FontWeight.Light,
        maxLines = 1,
        softWrap = false,
        style = TextStyle(fontFeatureSettings = "tnum"),
    )
}

fun timeParts(h: Int, m: Int, is24: Boolean): Pair<String, String> {
    val t = LocalTime.of(h, m)
    return if (is24) t.format(DateTimeFormatter.ofPattern("HH:mm")) to ""
    else t.format(DateTimeFormatter.ofPattern("h:mm")) to (if (h < 12) "am" else "pm")
}

fun clockParts(t: ZonedDateTime, is24: Boolean, seconds: Boolean): Pair<String, String> {
    val pattern = (if (is24) "HH" else "h") + ":mm" + (if (seconds) ":ss" else "")
    return t.format(DateTimeFormatter.ofPattern(pattern)) to (if (is24) "" else if (t.hour < 12) "am" else "pm")
}

fun describeDays(days: Int): String = when (days) {
    0 -> "Once"
    127 -> "Every day"
    else -> (0..6).filter { ((days shr it) and 1) == 1 }
        .joinToString(" ") { DayOfWeek.of(it + 1).getDisplayName(DayTextStyle.SHORT, Locale.getDefault()) }
}

/** 1:05:09, 4:07 or just 58 (under a minute), like a kitchen timer. */
fun formatRing(ms: Long): String {
    val s = (ms + 999) / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return when {
        h > 0 -> "%d:%02d:%02d".format(h, m, sec)
        m > 0 -> "%d:%02d".format(m, sec)
        else -> "$sec"
    }
}

fun timerLabel(totalMs: Long): String {
    val s = totalMs / 1000
    return when {
        s % 3600 == 0L -> "${s / 3600} h timer"
        s % 60 == 0L -> "${s / 60} m timer"
        else -> formatRing(totalMs) + " timer"
    }
}

fun formatStopwatch(ms: Long): String {
    val cs = ms / 10 % 100
    val s = ms / 1000 % 60
    val m = ms / 60000
    return "%02d:%02d.%02d".format(m, s, cs)
}
