package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.LuaBox
import com.shinonometn.kt.luabox.LuaBoxEarlyExitException
import com.shinonometn.kt.luabox.createLuaBoxEnvironment
import org.junit.Test
import org.luaj.vm2.LuaError
import kotlin.test.assertEquals

class RestrictedOsLibTest {

    private val luabox = LuaBox.default()
    @Test
    fun `Test getenv`() {

        val value = luabox.load("""require "os"; return os.getenv("ENV")""", createLuaBoxEnvironment {
            useRequire(mapOf(
                "os" to LuaBox.luaLibOS(mapOf("ENV" to "VALUE"))
            ))
        }).call()

        assertEquals("VALUE", value.tojstring(), "Should be 'VALUE'")
    }

    @Test(expected = LuaBoxEarlyExitException::class)
    fun `Test exit`() {
        try {
            luabox.load("""require "os"; os.exit(); return os.getenv("ENV")""", createLuaBoxEnvironment {
                useRequire(mapOf(
                    "os" to LuaBox.luaLibOS(mapOf("ENV" to "VALUE"))
                ))
            }).call()
        } catch (e: LuaError) {
            val cause = e.cause
            if (cause != null && cause is LuaBoxEarlyExitException) throw cause
        }
    }
}