package com.shinonometn.kt.luabox

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction

@LuaBoxDsl
fun luaPackage(name : String, body : (Globals) -> LuaValue) : TwoArgFunction = object : TwoArgFunction() {
    override fun tojstring(): String = name

    override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
        val globals = arg2.checkglobals()
        val table = body(globals)
        return globals.registerPackage(name, table)
    }
}

/**
 * A function that provides package init
 * @param name what name will this package be in lua
 * @param packageConfig what actions the package init will do
 * @return a lua function to init this package
 */
@LuaBoxDsl
fun luaBoxPackage(name : String, packageConfig : (LuaTable) -> LuaValue) = varargLuaFunction(name) { args ->
    val environment = args.arg(2).checktable()
    environment.assertIsEnvironment()
    environment.registerPackage(name, packageConfig(environment))
}

abstract class LuaBoxPackage(val name : String) : TwoArgFunction() {

    override fun tojstring() = name

    override fun call(arg1: LuaValue, env: LuaValue): LuaValue {
        val globals = env.checkglobals()
        val table = providePackage(globals)
        return globals.registerPackage(name, table)
    }

    protected abstract fun providePackage(globals: Globals) : LuaValue
}

class LazyLuaFunction(provider: () -> LuaFunction) : VarArgFunction() {
    private val lazyValue by lazy(provider)
    override fun invoke(args : Varargs): Varargs = lazyValue(args)
}

fun lazyLuaFunction(provider : () -> LuaFunction) = LazyLuaFunction(provider)