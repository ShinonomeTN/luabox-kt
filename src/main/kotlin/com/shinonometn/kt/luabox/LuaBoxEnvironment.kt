package com.shinonometn.kt.luabox

import com.shinonometn.kt.luabox.lib.luaFunctionPackageLibLoader
import com.shinonometn.kt.luabox.lib.luaFunctionStandardPrint
import com.shinonometn.kt.luabox.lib.luaLibBase
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.LibFunction
import java.io.PrintStream

class LuaBoxEnvironment : LuaTable()

class LuaBoxEnvironmentConfiguration internal constructor() {

    internal val valueProviders = mutableMapOf<String, (LuaBoxEnvironment) -> LuaValue>()

    @LuaBoxDsl
    fun initialValueProviders(vararg pairs: Pair<String, (LuaBoxEnvironment) -> LuaValue>) = valueProviders.putAll(pairs)

    @LuaBoxDsl
    @JvmName("initialLuaValues")
    fun initialValues(vararg pairs: Pair<String, LuaValue>) = valueProviders.putAll(pairs.map { (key, value) -> key to { value } })

    @LuaBoxDsl
    fun initialValue(name: String, value: LuaValue) = valueProviders.set(name) { value }

    @LuaBoxDsl
    fun initialValue(name: String, provider: (LuaBoxEnvironment) -> LuaValue) = valueProviders.set(name, provider)

    /**
     * Setup standard print function's output destination
     * @param stdin given a output
     */
    @LuaBoxDsl
    fun standardPrintTo(stdin: PrintStream) = initialValue("print") {
        LuaBox.luaFunctionStandardPrint({ stdin }, it)
    }

    internal val actions = mutableListOf<(LuaBoxEnvironment) -> Unit>(
        { it.load(LuaBox.luaLibBase()) }
    )

    @LuaBoxDsl
    fun initialAction(action: (LuaBoxEnvironment) -> Unit) = actions.add(action)

    /**
     * Allow to use 'require' function
     * @param withPreloads add those package loaders to preload table, key is package name
     */
    @LuaBoxDsl
    fun useRequire(withPreloads : Map<String, LibFunction> = emptyMap()) = initialAction {
        LuaBox.luaFunctionPackageLibLoader(it, withPreloads).invoke()
    }

    /**
     * Allow to use preloaded packages
     */
    @LuaBoxDsl
    fun preloadPackages(vararg preloads : Pair<String, LibFunction>) = useRequire(preloads.toMap())

    /**
     * Allow to use preloaded packages
     */
    @LuaBoxDsl
    fun preloadPackages(vararg libLoaders : LuaBoxJavaFunction) = useRequire(libLoaders.associateBy { it.name() })
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

    if(conf.valueProviders.isNotEmpty()) {
        val keyValues = conf.valueProviders.entries.map { (key, provider) -> key to provider(environment) }
        keyValues.forEach { environment[it.first] = it.second }
    }

    if(conf.actions.isNotEmpty()) conf.actions.forEach { it(environment) }

    return environment
}