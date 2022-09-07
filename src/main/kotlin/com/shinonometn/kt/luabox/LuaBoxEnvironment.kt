package com.shinonometn.kt.luabox

import com.shinonometn.kt.luabox.lib.luaBoxLibBase
import com.shinonometn.kt.luabox.lib.luaBoxLibPackage
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.LibFunction

class LuaBoxEnvironment : LuaTable()

class LuaBoxEnvironmentConfiguration internal constructor() {
    private val preloaded = mutableMapOf<String, LibFunction>()
    fun preloads(vararg preloads: Pair<String, LibFunction>): Unit = preloaded.putAll(preloads)

    var enableDebug: Boolean = false
    var enableStandardPrint: Boolean = false

    internal val initialValues = mutableListOf<(LuaTable) -> LuaValue>(
        { luaBoxLibBase() }
    )

    fun allowRequire() = initialValues.add { luaBoxLibPackage(it, preloaded) }
}

fun LuaTable.assertIsEnvironment() {
    if ((this !is LuaBoxEnvironment) && (this !is Globals))
        throw IllegalArgumentException("environment should be a lua box environment or lua global")
}

fun LuaTable.registerPackage(name: String, table: LuaValue): LuaValue {
    assertIsEnvironment()
    set(name, table.checktable())
    get("package")["loaded"][name] = table
    return table
}

/**
 * Create a lua environment, the GLOBAL of a lua fragment.
 */
@LuaBoxDsl
fun createLuaEnvironment(configuration: (LuaBoxEnvironmentConfiguration.() -> Unit)? = null): LuaBoxEnvironment {
    val conf = LuaBoxEnvironmentConfiguration().also { configuration?.invoke(it) }
    val environment = LuaBoxEnvironment()
    conf.initialValues.forEach { environment.load(it(environment)) }
    return environment
}