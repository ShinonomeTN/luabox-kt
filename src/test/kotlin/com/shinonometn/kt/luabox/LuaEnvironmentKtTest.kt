package com.shinonometn.kt.luabox

import org.junit.Test
import org.luaj.vm2.LuaError
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.test.assertEquals

internal class LuaEnvironmentKtTest{

    private val luabox = LuaBox.default()

    @Test
    fun `Test minimal global`() {
        val result = luabox.load("local a = 0; local b = 0; return a + b", createLuaEnvironment()).call()
        assertEquals(0, result.checkint())
    }

    @Test(expected = LuaError::class)
    fun `Test jail`() {
        luabox.load("print \"hello\"", createLuaEnvironment()).call()
    }

    @Test
    fun `Test jail repeat`() {
        val executor = Executors.newFixedThreadPool(4)
        (0..10000).map {
            executor.submit {
                val number1 = Random.nextInt(0, 100)
                val number2 = Random.nextInt(0, 100)
                val value = luabox.load("a = $number1; b = $number2; return a + b", createLuaEnvironment()).call()
                assertEquals(number1 + number2, value.checkint(), "Should equals.")
            }
        }.forEach { it.get() }
    }
}