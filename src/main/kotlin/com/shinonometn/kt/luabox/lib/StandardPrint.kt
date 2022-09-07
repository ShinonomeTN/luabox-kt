package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.LuaBox
import com.shinonometn.kt.luabox.LuaBoxEnvironment
import com.shinonometn.kt.luabox.LuaBoxFunc
import com.shinonometn.kt.luabox.varargLuaFunction
import org.luaj.vm2.LuaValue
import java.io.PrintStream

/**
 * Give a standard print function
 * @param stdoutProvider provider that gives the stdout be used
 */
@LuaBoxFunc
fun LuaBox.Companion.luaFunctionStandardPrint(
    stdoutProvider: () -> PrintStream,
    environment: LuaBoxEnvironment
) = varargLuaFunction { args ->
    val tostring = environment["tostring"].takeUnless { it.isnil() }
    val stdout = stdoutProvider()
    var i = 1
    val n = args.narg()
    while (i <= n) {
        if (i > 1) stdout.print('\t')

        val current = args.arg(i++)
        val s = current.tostring()
        stdout.print((if(s.isnil()) (tostring?.call(current) ?: current) else s).checkjstring())
    }
    stdout.println()
    LuaValue.NONE
}