package com.shinonometn.kt.luabox

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

class LuaBoxEnvironment : LuaTable()

class LuaBoxEnvironmentConfiguration internal constructor() {

    internal val valueProviders = mutableMapOf<String, (LuaBoxEnvironment) -> LuaValue>()

    internal val actions = mutableListOf<(LuaBoxEnvironment) -> Unit>()

    @LuaBoxDsl
    fun initialValueProviders(vararg pairs: Pair<String, (LuaBoxEnvironment) -> LuaValue>) {
        valueProviders.putAll(pairs)
    }

    @LuaBoxDsl
    @JvmName("initialLuaValues")
    fun initialValues(vararg pairs: Pair<String, LuaValue>) {
        valueProviders.putAll(pairs.map { (key, value) -> key to { value } })
    }

    @LuaBoxDsl
    fun initialValue(name: String, value: LuaValue) {
        valueProviders[name] = { value }
    }

    @LuaBoxDsl
    fun initialValue(name: String, provider: (LuaBoxEnvironment) -> LuaValue) {
        valueProviders[name] = provider
    }

    @LuaBoxDsl
    fun initialAction(action: (LuaBoxEnvironment) -> Unit) {
        actions.add(action)
    }
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
 * Create a lua environment, the GLOBAL for a lua fragment.
 */
@LuaBoxDsl
fun createLuaBoxEnvironment(configuration: (LuaBoxEnvironmentConfiguration.() -> Unit)? = null): LuaBoxEnvironment {
    val conf = LuaBoxEnvironmentConfiguration().also { configuration?.invoke(it) }
    val environment = LuaBoxEnvironment()

    if (conf.valueProviders.isNotEmpty()) {
        val keyValues = conf.valueProviders.entries.map { (key, provider) -> key to provider(environment) }
        keyValues.forEach { environment[it.first] = it.second }
    }

    if (conf.actions.isNotEmpty()) conf.actions.forEach { it(environment) }

    return environment
}