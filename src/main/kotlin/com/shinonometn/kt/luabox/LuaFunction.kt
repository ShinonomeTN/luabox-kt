package com.shinonometn.kt.luabox

import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction
import org.luaj.vm2.lib.ZeroArgFunction

open class LuaBoxJavaFunction(
    private val name: String = "JavaFunction",
    private val implementation: LuaBoxJavaFunction.(Varargs) -> Varargs = { NIL }
) : VarArgFunction() {
    override fun tojstring() = toString()
    override fun invoke(args: Varargs): Varargs = implementation(args)
}

@LuaBoxDsl
fun zeroArgLuaFunction(
    name: String = "OneArgJavaFunction",
    body: LuaBoxJavaFunction.() -> LuaValue
) = LuaBoxJavaFunction(name) { body() }

@LuaBoxDsl
fun oneArgLuaFunction(
    name: String = "OneArgJavaFunction",
    body: LuaBoxJavaFunction.(LuaValue) -> LuaValue
) = LuaBoxJavaFunction(name) { body(it.arg1()) }

@LuaBoxDsl
fun twoArgLuaFunction(
    name: String = "TwoArgsJavaFunction",
    body: LuaBoxJavaFunction.(LuaValue, LuaValue) -> LuaValue
) = LuaBoxJavaFunction(name) { body(it.arg1(), it.arg(2)) }

@LuaBoxDsl
fun threeArgLuaFunction(
    name: String = "ThreeArgsJavaFunction",
    body: LuaBoxJavaFunction.(LuaValue, LuaValue, LuaValue) -> LuaValue
) = LuaBoxJavaFunction(name) { body(it.arg1(), it.arg(2), it.arg(3)) }

@LuaBoxDsl
fun varargLuaFunction(
    name: String = "VarArgJavaFunction",
    body: LuaBoxJavaFunction.(Varargs) -> Varargs
) = LuaBoxJavaFunction(name) { body(it) }