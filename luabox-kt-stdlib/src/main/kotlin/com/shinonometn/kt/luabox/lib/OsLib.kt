package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.*
import org.luaj.vm2.LuaTable
import org.luaj.vm2.lib.VarArgFunction
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

private const val C_YEAR = Calendar.YEAR
private const val C_MONTH = Calendar.MONTH
private const val C_HOUR = Calendar.HOUR_OF_DAY
private const val C_DAY = Calendar.DAY_OF_MONTH
private const val C_WEEKDAY = Calendar.DAY_OF_WEEK
private const val C_MINUTE = Calendar.MINUTE
private const val C_SECOND = Calendar.SECOND

private const val C_MILLISECOND = Calendar.MILLISECOND

private val TMP_PREFIX = ".luaj"
private val TMP_SUFFIX = "tmp"

private val WeekdayNameAbbrev = arrayOf(
    "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
)
private val WeekdayName = arrayOf(
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
)
private val MonthNameAbbrev = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)
private val MonthName = arrayOf(
    "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"
)

val tempFileCounter = AtomicLong(0)

private val staticMethods = mapOf(
    "exit" to varargLuaFunction {
        val code = it.arg1().takeIf { l -> l.isnumber() }?.toint() ?: 0
        luaExit(code)
    },

    "tmpname" to zeroArgLuaFunction {
        val id = tempFileCounter.accumulateAndGet(1) { pre, add -> (pre % Long.MAX_VALUE) + add }
        val time = System.currentTimeMillis()
        VarArgFunction.valueOf("${TMP_PREFIX}${String.format("%016x%016x", time, id)}${TMP_SUFFIX}")
    },

    "difftime" to twoArgLuaFunction { arg1, arg2 ->
        VarArgFunction.valueOf(arg2.checkdouble() - arg1.checkdouble())
    },

    "time" to varargLuaFunction {
        VarArgFunction.valueOf(time(it.opttable(1, null)))
    },

    "date" to varargLuaFunction {
        val s = it.optjstring(1, "%c")
        val t = if (it.isnumber(2)) it.todouble(2) else time(null)
        if (s.equals("*t")) {
            val calendar = Calendar.getInstance().apply { time = Date((t * 1000).toLong()) }
            luaTableOf(
                "year" to VarArgFunction.valueOf(calendar[C_YEAR]),
                "month" to VarArgFunction.valueOf(calendar[C_MONTH] + 1),
                "day" to VarArgFunction.valueOf(calendar[C_DAY]),
                "hour" to VarArgFunction.valueOf(calendar[C_HOUR]),
                "min" to VarArgFunction.valueOf(calendar[C_MINUTE]),
                "sec" to VarArgFunction.valueOf(calendar[C_SECOND]),
                "wday" to VarArgFunction.valueOf(calendar[C_WEEKDAY]),
                "yday" to VarArgFunction.valueOf(calendar[0x6]),
                "isdst" to VarArgFunction.valueOf(isDaylightSavingTime(calendar))
            )
        } else {
            VarArgFunction.valueOf(date(s, if (t.toInt() == -1) time(null) else t))
        }
    }
)

private fun time(table: LuaTable?): Double = (if (table == null) Date() else Calendar.getInstance().apply {
    set(C_YEAR, table["year"].checkint())
    set(C_MONTH, table["month"].checkint() - 1)
    set(C_DAY, table["day"].checkint())
    set(C_HOUR, table["hour"].checkint())
    set(C_MINUTE, table["min"].optint(12))
    set(C_SECOND, table["sec"].optint(0))
    set(C_MILLISECOND, 0)
}.time).time / 1000.0

private fun date(givenFormat: String, givenTime: Double): String {
    val calendar = Calendar.getInstance().apply { time = Date((givenTime * 1000).toLong()) }

    val (time, format) = if (givenFormat.startsWith("!")) {
        val newTime = givenTime - timeZoneOffset(calendar)
        calendar.time = Date((newTime * 1000).toLong())
        (newTime to givenFormat.substring(1))
    } else (givenTime to givenFormat)

    val sb = StringBuilder()
    var convert = false
    for (char in format) when {
        convert -> {
            dateFormatTable[char]?.invoke(sb, calendar, time) ?: VarArgFunction.argerror(1, "invalid conversion specifier '%$char'")
            convert = false
            continue
        }

        char == '%' -> {
            convert = true
            continue
        }

        else -> sb.append(char)
    }

    return sb.toString()
}

private val dateFormatTable = mapOf<Char, StringBuilder.(Calendar, Double) -> Unit>(
    '%' to { _, _ -> append('%') },
    'a' to { it, _ -> append(WeekdayNameAbbrev[it[C_WEEKDAY] - 1]) },
    'A' to { it, _ -> append(WeekdayName[it[C_WEEKDAY] - 1]) },
    'b' to { it, _ -> append(MonthNameAbbrev[it[C_MONTH]]) },
    'B' to { it, _ -> append(MonthName[it[C_MONTH]]) },
    'c' to { _, it -> append(date("%a %b %d %H:%M:%S %Y", it)) },
    'd' to { it, _ -> append((100 + it[C_DAY]).toString().substring(1)) },
    'H' to { it, _ -> append((100 + it[C_HOUR]).toString().substring(1)) },
    'I' to { it, _ -> append((100 + (it[C_HOUR] % 12)).toString().substring(1)) },
    'j' to { it, _ ->
        val beginningOfYear = it.beginningOfYear()
        val dayOfYear = (it.time.time - beginningOfYear.time.time) / (24 * 3600 * 1000)
        append((1001 + dayOfYear).toString().substring(1))
    },
    'm' to { it, _ -> append((101 + it[C_MONTH]).toString().substring(1)) },
    'M' to { it, _ -> append((100 + it[C_MINUTE]).toString().substring(1)) },
    'p' to { it, _ -> append(if (it[C_HOUR] < 12) "AM" else "PM") },
    'S' to { it, _ -> append((100 + it[C_SECOND]).toString().substring(1)) },
    'U' to { it, _ -> append(weekNumber(it, 0).toString()) },
    'w' to { it, _ -> append(((it[C_WEEKDAY] + 6) % 7).toString()) },
    'W' to { it, _ -> append(weekNumber(it, 1).toString()) },
    'x' to { _, it -> append(date("%m/%d/%y", it)) },
    'X' to { _, it -> append(date("%H:%M:%S", it)) },
    'y' to { it, _ -> append(it[C_YEAR].toString().substring(2)) },
    'T' to { it, _ -> append(it[C_YEAR].toString()) },
    'z' to { it, _ ->
        val offset = timeZoneOffset(it) / 60;
        val abs = abs(offset)
        val hour = (100 + abs / 60).toString().substring(1)
        val minute = (100 + abs % 60).toString().substring(1);
        append((if (offset >= 0) "+" else "-") + "$hour$minute")
    }
)

private val calendarResetSequence = listOf(
    C_MONTH to 0,
    C_DAY to 1,
    C_HOUR to 0,
    C_MINUTE to 0,
    C_SECOND to 0,
    C_MILLISECOND to 0,
)

private fun Calendar.beginningOfYear() = Calendar.getInstance().apply {
    time = this@beginningOfYear.time
    calendarResetSequence.forEach { set(it.first, it.second) }
}

private fun weekNumber(calendar: Calendar, startDay: Int): Int {
    val year0 = calendar.beginningOfYear()
    year0[C_DAY] = 1 + (startDay + 8 - year0[C_WEEKDAY]) % 7
    if (year0.after(calendar)) {
        year0[C_YEAR] = year0[C_YEAR] - 1
        year0[C_DAY] = 1 + (startDay + 8 - year0[C_WEEKDAY]) % 7
    }
    val diff = calendar.time.time - year0.time.time
    return (1 + (diff / (7 * 24 * 3600 * 1000))).toInt()
}

private fun isDaylightSavingTime(calendar: Calendar) = calendar.timeZone.rawOffset / 1000

private fun timeZoneOffset(c: Calendar): Int {
    val localStandardTimeMillis = (c[C_HOUR] * 3600 + c[C_MINUTE] * 60 + c[C_SECOND]) * 1000
    return c.timeZone.getOffset(1, c[C_YEAR], c[C_MONTH], c[C_DAY], c[C_WEEKDAY], localStandardTimeMillis) / 1000
}

/**
 * Create a basic os lib
 * @param systemEnv a map that containing environment-variable-like values
 * @param time0 set the base time of 'clock' function
 * @return a lua function to install this package to lua environment
 */
@LuaBoxLib
fun LuaBox.Companion.luaLibOS(systemEnv: Map<String, String> = emptyMap(), time0: Long = System.currentTimeMillis()) = luaBoxPackage("os") { env ->
    val dynamicMethods = mapOf(
        "getenv" to oneArgLuaFunction { (systemEnv[it.checkjstring()] ?: "").toLuaValue() },
        "clock" to zeroArgLuaFunction { ((System.currentTimeMillis() - time0) / 1000.0).toLuaValue() }
    )

    val table = (dynamicMethods + staticMethods).toLuaTable().setmetatable(
        luaTableOf(VarArgFunction.METATABLE to true.toLuaValue())
    )

    env["os"] = table
    env.registerPackage("os", table)

    table
}