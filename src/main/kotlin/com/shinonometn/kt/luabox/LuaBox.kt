package com.shinonometn.kt.luabox

import org.luaj.vm2.*
import org.luaj.vm2.compiler.LuaC
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream

class LuaBox private constructor(
    val unDumper: Globals.Undumper? = LoadState.instance,
    val loader: Globals.Loader = LuaC.instance,
    val compiler: Globals.Compiler = LuaC.instance
) {

    enum class LoadMode { BINARY, TEXT }

    /**
     * Convenience function to load a string for execution
     * @param string lua script text
     * @param environment the global environment
     * @return an executable lua function
     */
    fun load(string : String, environment: LuaValue) = load(ByteArrayInputStream(string.toByteArray()), string, "t" ,environment)

    /**
     * Load a [prototype] with given [environment] and [chunkName]
     * @return an executable lua function
     * If using LuaJC, [chunkName] should be meaningful
     */
    fun load(prototype: Prototype, chunkName: String, environment: LuaValue) = loader.load(prototype, chunkName, environment)

    /**
     * Load a [inputStream] with given [environment] and [chunkName]
     * using given [mode]
     * @param mode 'b' or 't' or both. 'b' means lua bytecode, 't' means plain text lua code
     * @return an executable lua function
     * If using LuaJC, [chunkName] should be meaningful
     */
    fun load(inputStream: InputStream, chunkName: String, mode: String, environment: LuaValue): LuaValue {
        try {
            val prototype = loadAsPrototype(inputStream, chunkName, mode)
            return loader.load(prototype, chunkName, environment)
        } catch (e: Exception) {
            if (e is LuaError) throw e
            luaError("could not load chunk '$chunkName': ${e::class.qualifiedName}, ${e.message}")
        }
    }

    /**
     * Load a [inputStream] with given [chunkName] using given [mode]
     * @param mode 'b' or 't' or both. 'b' means lua bytecode, 't' means plain text lua code
     * @return a prototype can be load as lua function
     * If using LuaJC, [chunkName] should be meaningful
     */
    fun loadAsPrototype(inputStream: InputStream, chunkName: String, mode: String): Prototype {
        var input = inputStream
        val modes = mode.readLoadModes()
        // Try to read as binary
        if (modes.contains(LoadMode.BINARY)) {
            if (unDumper == null) luaError("No undumper set")
            if (!inputStream.markSupported()) input = BufferedInputStream(inputStream)
            input.mark(4)
            val prototype = unDumper.undump(input, chunkName)
            if (prototype != null) return prototype
            input.reset()
        }
        // If not, fallback to text mode
        if (modes.contains(LoadMode.TEXT)) return compileToPrototype(input, chunkName)
        // Otherwise, could not process
        luaError("Failed to load prototype '$chunkName' using mode '$mode'")
    }

    /**
     * Compile a [inputStream] with given [chunkName]
     * @return a prototype can be load as lua function (usually lua bytecode)
     * If using LuaJC, [chunkName] should be meaningful
     */
    fun compileToPrototype(inputStream: InputStream, chunkName: String): Prototype {
        return compiler.compile(inputStream, chunkName)
    }

    private fun String.readLoadModes(): Set<LoadMode> = mapNotNull {
        when (it) {
            't' -> LoadMode.TEXT
            'b' -> LoadMode.BINARY
            else -> null
        }
    }.toSet()

    companion object {
        fun default() = LuaBox()

        fun withoutByteCodeLoader() = LuaBox(null)

        class LuaBoxConfigurator internal constructor() {
            var unDumper : Globals.Undumper? = null
            var compiler : Globals.Compiler = LuaC.instance
            var loader : Globals.Loader = LuaC.instance
        }

        fun customized(config : LuaBoxConfigurator.() -> Unit) : LuaBox = LuaBoxConfigurator()
            .apply(config)
            .let { LuaBox(it.unDumper, it.loader, it.compiler) }
    }
}