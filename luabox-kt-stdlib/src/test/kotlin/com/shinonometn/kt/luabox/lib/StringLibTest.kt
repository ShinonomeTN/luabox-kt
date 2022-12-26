package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.LuaBox
import com.shinonometn.kt.luabox.createLuaBoxEnvironment
import org.junit.Test
import org.luaj.vm2.LuaValue

class StringLibTest {
    @Test
    fun `Test string lib sealing`() {
        val box = LuaBox.default()
        val result = box.load("""
            string.format("%s world", "Hello")
        """.trimIndent(), createLuaBoxEnvironment {
            useBaseLib()
            initialValue("string") { LuaBox.luaLibString().call(LuaValue.EMPTYSTRING, it) }
        })()
        println(result)
    }
}