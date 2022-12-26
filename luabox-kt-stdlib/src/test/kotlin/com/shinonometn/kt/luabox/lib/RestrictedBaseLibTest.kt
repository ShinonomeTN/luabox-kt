package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.LuaBox
import com.shinonometn.kt.luabox.createLuaBoxEnvironment
import org.junit.Test
import org.luaj.vm2.LuaError
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestrictedBaseLibTest {

    private val luabox = LuaBox.default()

    private fun luaBoxEnvironmentWithBaseLib() = createLuaBoxEnvironment {
        useBaseLib()
    }

    @Test
    fun `Test assert true`() {
        val assertTrue = luabox.load("assert(true)", luaBoxEnvironmentWithBaseLib()).call()
        assertTrue(assertTrue.isnil(), "Assert true should pass")
    }

    @Test(expected = LuaError::class)
    fun `Test assert false`() {
        luabox.load("assert(false)", luaBoxEnvironmentWithBaseLib()).call()
    }

    @Test
    fun `Test get and set metatable`() {
        val global = luaBoxEnvironmentWithBaseLib()

        val value1 = luabox.load("local table = {}; return getmetatable(table)", global).call()
        assertTrue(value1.isnil(), "get table here should returns nil")

        val value2 = luabox.load("local table = {}; setmetatable(table, {}); return getmetatable(table)", global).call()
        assertTrue(value2.istable(), "get table here should returns table")
    }

    @Test(expected = LuaError::class)
    fun `Test change protected metatable`() {
        val global = luaBoxEnvironmentWithBaseLib()
        luabox.load("local table = setmetatable({}, { __metatable = {} }); return setmetatable(table,{})", global).call()
    }

    @Test
    fun `Test rawequal`() {
        val global = luaBoxEnvironmentWithBaseLib()
        val value1 = luabox.load("local a = {}; local b = a; return rawequal(a,b)", global).call()
        assertTrue(value1.isboolean() && value1.toboolean(), "should returns true")

        val value2 = luabox.load("local a = {}; local b = {}; return rawequal(a,b)", global).call()
        assertTrue(value2.isboolean() && !value2.toboolean(), "should returns false")
    }

    @Test
    fun `Test rawget`() {
        val global = luaBoxEnvironmentWithBaseLib()
        val value1 = luabox.load("""
            a = setmetatable({}, { __index = function(self, key) return 1; end })
            return rawget(a, "a")
        """.trimIndent(), global).call()
        assertTrue(value1.isnil(), "Should equals to 'nil'")
        val value2 = luabox.load("return a.a", global).call()
        assertEquals(1, value2.checkint(), "Should equals to '1'")
    }

    @Test
    fun `Test rawset`() {
        val global = luaBoxEnvironmentWithBaseLib()
        val value1 = luabox.load("""
            a = setmetatable({}, { __newindex = function(self, key, value) return 1; end })
            rawset(a, "a", 1)
            return a.a
        """.trimIndent(), global).call()
        assertEquals(1, value1.checkint(), "Should equals 1")
        val value2  = luabox.load("a.b = 2; return a.b", global).call()
        assertTrue(value2.isnil(), "Should be nil")
    }
}