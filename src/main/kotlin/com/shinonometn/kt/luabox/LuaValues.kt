package com.shinonometn.kt.luabox

import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaUserdata
import org.luaj.vm2.LuaValue

/*
 * Value Helpers
 */

fun Int.toLuaValue() = LuaValue.valueOf(this)

fun String.toLuaValue() = LuaValue.valueOf(this)

fun Boolean.toLuaValue() = LuaValue.valueOf(this)

fun Double.toLuaValue() = LuaValue.valueOf(this)

fun Any.toLuaUserData(metaTable : LuaValue? = null) =
    if(metaTable != null) LuaUserdata(this, metaTable)
    else LuaUserdata(this)

/*
 * Table helpers
 */

fun LuaTable.putAll(vararg pairs : Pair<LuaValue, LuaValue>) = pairs.forEach { set(it.first, it.second) }

fun LuaTable.putAll(map : Map<LuaValue, LuaValue>) = map.forEach { (key, value) -> set(key, value) }

@JvmName("putAllStringLuaValue")
fun LuaTable.putAll(map : Map<String, LuaValue>) = map.forEach { (key, value) -> set(key, value) }

@JvmName("putAllStringLuaValue")
fun LuaTable.putAll(vararg pairs : Pair<String, LuaValue>) = pairs.forEach { (key, value) -> set(key, value) }

fun Map<String, LuaValue>.toLuaTable() = LuaTable().apply { entries.forEach { set(it.key, it.value) } }

@JvmName("asLuaTable")
fun Map<LuaValue, LuaValue>.toLuaTable() = LuaTable().apply { entries.forEach { set(it.key, it.value) } }

fun Collection<Pair<LuaValue, LuaValue>>.toLuaTable() = LuaTable().apply { forEach { (key, value) -> set(key, value) } }

@JvmName("asLuaTable")
fun Collection<Pair<String, LuaValue>>.toLuaTable() = LuaTable().apply { forEach { (key, value) -> set(key, value) } }

fun luaTableOf() = LuaTable()

fun luaTableOf(vararg entries : Pair<String, LuaValue>) : LuaTable {
    val table = LuaTable()
    if(entries.isNotEmpty()) entries.forEach { (key, value) -> table[key] = value }
    return table
}

@JvmName("luaTableFrom")
fun luaTableOf(vararg entries : Pair<LuaValue, LuaValue>) : LuaTable {
    val table = LuaTable()
    if(entries.isNotEmpty()) entries.forEach { (key, value) -> table[key] = value }
    return table
}

/*
 * Array Helper
 */
fun LuaTable.addAll(vararg values : LuaValue) = values.forEachIndexed { index, value -> set(index + 1, value) }

fun luaListOf(vararg values : LuaValue) = LuaTable().apply { values.forEachIndexed { index, luaValue -> set(index + 1, luaValue) } }

