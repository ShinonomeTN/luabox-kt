package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.LuaBox
import com.shinonometn.kt.luabox.LuaBoxLib
import org.luaj.vm2.lib.StringLib

private val theStringLib = StringLib()

/**
 * The global StringLib instance for LuaBox
 */
@LuaBoxLib
fun LuaBox.Companion.luaLibString() = theStringLib