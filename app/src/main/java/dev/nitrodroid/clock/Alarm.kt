package dev.nitrodroid.clock

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZonedDateTime

/** days is a bitmask: bit 0 = Monday ... bit 6 = Sunday. 0 means "once". */
data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val days: Int = 0,
    val enabled: Boolean = true,
)

fun Alarm.nextTrigger(now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
    val base = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    for (i in 0..7) {
        val candidate = base.plusDays(i.toLong())
        val dayOk = days == 0 || ((days shr (candidate.dayOfWeek.value - 1)) and 1) == 1
        if (dayOk && candidate.isAfter(now)) return candidate
    }
    return base.plusDays(1)
}

/** Everything is kept in one small SharedPreferences file so the app has no database dependency. */
object AlarmStore {
    private fun p(c: Context) = c.getSharedPreferences("nitro_clock", Context.MODE_PRIVATE)

    fun load(c: Context): List<Alarm> {
        val arr = JSONArray(p(c).getString("alarms", "[]"))
        return List(arr.length()) {
            val o = arr.getJSONObject(it)
            Alarm(o.getInt("id"), o.getInt("h"), o.getInt("m"), o.optString("l", ""), o.optInt("d", 0), o.optBoolean("e", true))
        }
    }

    fun save(c: Context, list: List<Alarm>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject().put("id", it.id).put("h", it.hour).put("m", it.minute)
                    .put("l", it.label).put("d", it.days).put("e", it.enabled)
            )
        }
        p(c).edit().putString("alarms", arr.toString()).apply()
    }

    fun nextId(c: Context): Int {
        val n = p(c).getInt("next", 1)
        p(c).edit().putInt("next", n + 1).apply()
        return n
    }

    fun timerEnd(c: Context): Long = p(c).getLong("timerEnd", 0L)
    fun timerTotal(c: Context): Long = p(c).getLong("timerTotal", 0L)
    fun setTimer(c: Context, end: Long, total: Long) {
        p(c).edit().putLong("timerEnd", end).putLong("timerTotal", total).apply()
    }

    fun zones(c: Context): List<String> =
        (p(c).getString("zones", null) ?: "Europe/London,America/New_York,Asia/Tokyo")
            .split(",").filter { it.isNotBlank() }

    fun saveZones(c: Context, zones: List<String>) {
        p(c).edit().putString("zones", zones.joinToString(",")).apply()
    }
}
