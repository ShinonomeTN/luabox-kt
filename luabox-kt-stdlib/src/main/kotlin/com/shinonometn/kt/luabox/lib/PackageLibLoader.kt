package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.*
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.LibFunction
import org.luaj.vm2.lib.VarArgFunction
import kotlin.properties.Delegates.notNull

private const val FILE_SEP = "/"

private val S_LOADED = LuaValue.valueOf("loaded");
private val S_LOAD_LIB = LuaValue.valueOf("loadlib")
private val S_PRELOAD = LuaValue.valueOf("preload")
private val S_PATH = LuaValue.valueOf("path")
private val S_SEARCH_PATH = LuaValue.valueOf("searchpath")
private val S_SEARCHERS = LuaValue.valueOf("searchers")
private val S_SENTINEL = LuaValue.valueOf("\u0001")

private val dummyLoadLib = varargLuaFunction {
    it.checkstring(1)
    VarArgFunction.varargsOf(VarArgFunction.NIL, VarArgFunction.valueOf("dynamic library loading is not supported"), VarArgFunction.valueOf("absent"))
}

private fun require(packageTable: LuaTable) = oneArgLuaFunction {
    val name = it.checkstring()
    val loaded = packageTable[S_LOADED]
    var result = loaded[name]

    if (result.toboolean()) when {
        (result == S_SENTINEL) -> luaError("loop or previous error loading module '$name'")
        else -> return@oneArgLuaFunction result
    }

    val searchers = packageTable[S_SEARCHERS].checktable()
    val sb = StringBuilder()
    var loader: Varargs by notNull()
    var count = 1
    while (true) {
        val searcher = searchers[count++]
        if (searcher.isnil()) luaError("module '$name' not found: $name$sb")
        loader = searcher.invoke(name)
        if (loader.isfunction(1)) break;
        if (loader.isstring(1)) sb.append(loader.tojstring(1))
    }

    loaded[name] = S_SENTINEL
    result = loader.arg1().call(name, loader.arg(2))

    when {
        !result.isnil() -> loaded[name] = result
        loaded[name].also { result = it } === S_SENTINEL -> loaded[name] = VarArgFunction.TRUE.also { result = it }
    }
    result
}

private fun preloadSearcher(packageTable: LuaTable, environment: LuaTable) = varargLuaFunction { args ->
    val name = args.checkstring(1)
    val value = packageTable[S_PRELOAD][name]
    if (value.isnil()) VarArgFunction.valueOf("\n\tno field package.preload['$name']") else LuaValue.varargsOf(value, environment)
}

private fun luaFileSearcher(packageTable: LuaTable, environment: LuaTable) = varargLuaFunction { args ->
    val name = args.checkstring(1)
    // get package path
    val path = packageTable[S_PATH]
        .takeIf { it.isstring() }
        ?: return@varargLuaFunction VarArgFunction.valueOf("package.path is not a string")

    // get the searchpath function.
    val filename = packageTable[S_SEARCH_PATH](VarArgFunction.varargsOf(name, path)).arg1()
        .takeIf { it.isstring() }?.strvalue()
        ?: return@varargLuaFunction VarArgFunction.valueOf("package.path is not a string")

    val fileLoader = environment["loadfile"]
        .takeIf { it.isfunction() } ?: return@varargLuaFunction VarArgFunction.valueOf("loadfile is nil or not a function")

    val loaded = fileLoader(filename)

    if (loaded.arg1().isfunction()) VarArgFunction.varargsOf(loaded.arg1(), filename) else VarArgFunction.varargsOf(
        VarArgFunction.NIL,
        VarArgFunction.valueOf("'$filename': ${loaded.arg(2).tojstring()}")
    )
}

private fun searchPath(environment: LuaTable) = varargLuaFunction { args ->
    var name = args.checkjstring(1)
    val path = args.checkjstring(2)
    val sep = args.optjstring(3, ".")
    val rep = args.optjstring(4, FILE_SEP)

    val finder = environment["findpath"]
        .takeIf { it.isfunction() }
        ?: luaError("findpath is null or not a function")

    // check the path elements
    var e = -1
    val n = path.length
    val sb by lazy { StringBuilder() }
    name = name.replace(sep[0], rep[0])
    while (e < n) {

        // find next template
        val b = e + 1
        e = path.indexOf(';', b)
        if (e < 0) e = path.length
        val template = path.substring(b, e)

        // create filename
        val q = template.indexOf('?')
        var filename = template
        if (q >= 0) filename = template.substring(0, q) + name + template.substring(q + 1)

        // try opening the file
        val result = finder(VarArgFunction.valueOf(filename))
        if (!result.arg1().isnil()) return@varargLuaFunction result.arg1().checkstring()

        // report error

        sb.append("\n\t$filename")
    }

    VarArgFunction.varargsOf(VarArgFunction.NIL, VarArgFunction.valueOf(sb.toString()))
}

/**
 * Provide 'require' and package searching functionalities
 * @param environment root environment be installed into
 * @param withPreload optional preloaded packages
 */
@LuaBoxFunc
fun LuaBox.Companion.luaFunctionPackageLibLoader(environment: LuaTable, withPreload: Map<String, LibFunction>) = varargLuaFunction("package") {
    val packageTable = luaTableOf(
        S_LOADED to luaTableOf(),
        S_PATH to LuaValue.valueOf("."),
        S_PRELOAD to withPreload.toLuaTable(),
        S_LOAD_LIB to dummyLoadLib,
        S_SEARCH_PATH to searchPath(environment)
    )

    packageTable[S_SEARCHERS] = luaListOf(preloadSearcher(packageTable, environment), luaFileSearcher(packageTable, environment))

    environment.apply {
        putAll(
            "package" to packageTable,
            "require" to require(packageTable)
        )
        registerPackage(name(), packageTable)
    }
}