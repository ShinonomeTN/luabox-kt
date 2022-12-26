package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.LuaBox
import com.shinonometn.kt.luabox.LuaBoxDsl
import com.shinonometn.kt.luabox.LuaBoxEnvironmentConfiguration
import com.shinonometn.kt.luabox.LuaBoxJavaFunction
import org.luaj.vm2.lib.LibFunction
import java.io.PrintStream

/**
 * Load baseLib
 */
@LuaBoxDsl
fun LuaBoxEnvironmentConfiguration.useBaseLib() = initialAction { it.load(LuaBox.luaLibBase()) }

/**
 * Setup standard print function's output destination
 * @param stdin given a output
 */
@LuaBoxDsl
fun LuaBoxEnvironmentConfiguration.standardPrintTo(stdin: PrintStream) = initialValue("print") {
    LuaBox.luaFunctionStandardPrint({ stdin }, it)
}

/**
 * Allow to use 'require' function
 * @param withPreloads add those package loaders to preload table, key is package name
 */
@LuaBoxDsl
fun LuaBoxEnvironmentConfiguration.useRequire(withPreloads : Map<String, LibFunction> = emptyMap()) = initialAction {
    LuaBox.luaFunctionPackageLibLoader(it, withPreloads).invoke()
}

/**
 * Allow to use preloaded packages
 */
@LuaBoxDsl
fun LuaBoxEnvironmentConfiguration.preloadPackages(vararg preloads : Pair<String, LibFunction>) = useRequire(preloads.toMap())

/**
 * Allow to use preloaded packages
 */
@LuaBoxDsl
fun LuaBoxEnvironmentConfiguration.preloadPackages(vararg libLoaders : LuaBoxJavaFunction) = useRequire(libLoaders.associateBy { it.name() })