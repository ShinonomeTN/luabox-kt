package com.shinonometn.kt.luabox.lib

import org.luaj.vm2.LuaValue
import java.io.InputStream
import kotlin.properties.Delegates

class LuaStringInputStream(val func: LuaValue) : InputStream() {
    var offset = 0
    var remaining = 0
    var bytes: ByteArray by Delegates.notNull()

    override fun read(): Int {
        if (remaining <= 0) {
            val s = func.call()
            if (s.isnil()) return -1
            val ls = s.strvalue()
            bytes = ls.m_bytes
            offset = ls.m_offset
            remaining = ls.m_length
            if (remaining <= 0) return -1
        }
        --remaining
        return bytes[offset++].toInt()
    }
}