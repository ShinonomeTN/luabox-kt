package com.shinonometn.kt.luabox

import org.junit.Test
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import java.io.ByteArrayInputStream
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.test.assertEquals

internal class LuaEnvironmentKtTest{

    private val luabox = LuaBox.default()

    @Test
    fun `Test minimal global`() {
        val result = luabox.load("local a = 0; local b = 0; return a + b", createLuaBoxEnvironment()).call()
        assertEquals(0, result.checkint())
    }

    @Test
    fun `Test environment create`() {
        val environment = createLuaBoxEnvironment {
            initialAction { it["a"] = LuaValue.valueOf(1) }
            initialValue("b", LuaValue.valueOf(2))
            initialValues("c" to LuaValue.valueOf(3), "d" to LuaValue.valueOf(4))
            initialValueProviders("e" to { LuaValue.valueOf(5) }, "f" to { LuaValue.valueOf(6) })
        }

        listOf("a" to 1, "b" to 2, "c" to 3, "d" to 4, "e" to 5, "f" to 6).forEach {(key, expected) ->
            assertEquals(expected, environment[key].checkint(), "Check for $key:$expected failed.")
        }
    }

    @Test
    fun `Test prototype`() {
        val (a, b) = 1 to 2
        val prototype = luabox.compileToPrototype(ByteArrayInputStream("""return a + b""".toByteArray()), "main")
        val env1 = createLuaBoxEnvironment {
            initialValues("a" to a.toLuaValue(), "b" to b.toLuaValue())
        }
        val value1 = luabox.load(prototype, "main", env1).call()
        assertEquals(a + b, value1.checkint(), "Should equals 3")

        val (c, d) = 3 to 4
        val env2 = createLuaBoxEnvironment {
            initialValues("a" to c.toLuaValue(), "b" to d.toLuaValue())
        }
        val value2 = luabox.load(prototype, "main", env2).call()
        assertEquals(c + d, value2.checkint(), "Should equals 7")
    }

    @Test
    fun `Test jail repeat`() {
        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        (0..10000).map {
            executor.submit {
                val number1 = Random.nextInt(0, 100)
                val number2 = Random.nextInt(0, 100)
                val value = luabox.load("a = $number1; b = $number2; return a + b", createLuaBoxEnvironment()).call()
                assertEquals(number1 + number2, value.checkint(), "Should equals.")
            }
        }.forEach { it.get() }
    }
}