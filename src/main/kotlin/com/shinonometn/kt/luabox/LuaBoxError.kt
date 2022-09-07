package com.shinonometn.kt.luabox

import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue

@LuaBoxDsl
fun luaError(message: String): Nothing = throw LuaError(message)

@LuaBoxDsl
fun luaError(throwable: Throwable) : Nothing = throw LuaError(throwable)

@LuaBoxDsl
fun luaArgError(index : Int, message : String): Nothing {
    LuaValue.argerror(index, message)
    throw IllegalStateException()
}

class LuaBoxEarlyExitException(val exitCode : Int) : InterruptedException(exitCode.toString())

fun luaExit(exitCode: Int) : Nothing = throw LuaBoxEarlyExitException(exitCode)