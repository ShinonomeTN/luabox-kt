package com.shinonometn.kt.luabox

import org.luaj.vm2.LuaError

@LuaBoxDsl
fun luaError(message: String): Nothing = throw LuaError(message)

@LuaBoxDsl
fun luaError(throwable: Throwable) : Nothing = throw LuaError(throwable)

class LuaBoxEarlyExitException(val exitCode : Int) : InterruptedException(exitCode.toString())

fun luaExit(exitCode: Int) : Nothing = throw LuaBoxEarlyExitException(exitCode)