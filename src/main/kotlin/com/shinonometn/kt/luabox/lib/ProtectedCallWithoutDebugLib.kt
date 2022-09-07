package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.LuaBox
import com.shinonometn.kt.luabox.LuaBoxEarlyExitException
import com.shinonometn.kt.luabox.LuaBoxFunc
import com.shinonometn.kt.luabox.varargLuaFunction
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue

/**
 * A pcall implementation without debuglib
 */
@LuaBoxFunc
fun LuaBox.Companion.luaFunctionProtectedCallWithoutDebugLib() = varargLuaFunction { args ->
    val func = args.checkvalue(1)
    try {
        LuaValue.varargsOf(LuaValue.TRUE, func.invoke(args.subargs(2)))
    } catch (e: LuaBoxEarlyExitException) {
        throw e
    } catch (le: LuaError) {
        val cause = le.cause
        if (cause != null && cause is LuaBoxEarlyExitException) throw cause

        val m = le.messageObject
        LuaValue.varargsOf(LuaValue.FALSE, m ?: LuaValue.NIL)
    } catch (e: Exception) {
        val m = e.message
        LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf(m ?: e.toString()))
    }
}