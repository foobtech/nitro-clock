package dev.nitrodroid.clock.ui

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

fun formatTime(h: Int, m: Int): String =
    LocalTime.of(h, m).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

fun describeDays(days: Int): String = when (days) {
    0 -> "Once"
    127 -> "Every day"
    else -> (0..6).filter { ((days shr it) and 1) == 1 }
        .joinToString(" ") { DayOfWeek.of(it + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
}

fun formatCountdown(ms: Long): String {
    val s = (ms + 999) / 1000
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}

fun formatStopwatch(ms: Long): String {
    val cs = ms / 10 % 100
    val s = ms / 1000 % 60
    val m = ms / 60000
    return "%02d:%02d.%02d".format(m, s, cs)
}
