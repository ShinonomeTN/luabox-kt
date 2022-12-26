package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.*
import org.luaj.vm2.*
import org.luaj.vm2.lib.BaseLib
import java.io.PrintStream

/*
 * Re-implementation of BaseLib functions
 */

private val next = varargLuaFunction { args -> args.checktable(1).next(args.arg(2)) }

private val iNext = varargLuaFunction { args -> args.checktable(1).inext(args.arg(2)) }

private val staticFunctions = mapOf(
    // "assert", // ( v [,message] ) -> v, message | ERR
    "assert" to varargLuaFunction("assert") {
        if (!it.arg1().toboolean()) luaError(
            if (it.narg() > 1) it.optjstring(2, "assertion failed!")
            else "assertion failed!"
        )

        it
    },

    // "error", // ( message [,level] ) -> ERR
    "error" to twoArgLuaFunction("error") { arg1, arg2 ->
        throw when {
            arg1.isnil() -> LuaError(null, arg2.optint(1))
            arg1.isstring() -> LuaError(arg1.tojstring(), arg2.optint(1))
            else -> LuaError(arg1)
        }
    },

    "type" to oneArgLuaFunction { arg -> BaseLib.valueOf(arg.typename()) },

    "tonumber" to twoArgLuaFunction { e, base ->
        if (base.isnil()) e.tonumber()
        else {
            val b = base.checkint()
            if (b < 2 || b > 36) BaseLib.argerror(2, "base out of range")
            else e.checkstring().tonumber(b)
        }
    },
    "tostring" to oneArgLuaFunction { arg ->
        val h = arg.metatag(BaseLib.TOSTRING)
        if (!h.isnil()) h.call(arg)
        else {
            val v = arg.tostring()
            if (!v.isnil()) v else BaseLib.valueOf(arg.tojstring())
        }
    },

    // "getmetatable", // ( object ) -> table
    "getmetatable" to oneArgLuaFunction { arg ->
        if (arg.isnil()) BaseLib.argerror(1, "value")
        val mt = arg.getmetatable()
        if (mt != null) mt.rawget(BaseLib.METATABLE).optvalue(mt) else BaseLib.NIL
    },

    // "setmetatable", // (table, metatable) -> table
    "setmetatable" to twoArgLuaFunction { table, new ->
        when {
            new.isnil() -> BaseLib.argerror(2, "value")
            else -> {
                val origin = table.checktable().getmetatable()
                if (origin != null && !origin.rawget(BaseLib.METATABLE).isnil()) luaError("cannot change a protected metatable")
                table.setmetatable(if (new.isnil()) null else new.checktable())
            }
        }
    },

    // "rawequal", // (v1, v2) -> boolean
    "rawequal" to twoArgLuaFunction { arg1, arg2 ->
        when {
            arg1.isnil() -> BaseLib.argerror(1, "value")
            arg2.isnil() -> BaseLib.argerror(2, "value")
            else -> BaseLib.valueOf(arg1.raweq(arg2))
        }
    },

    // "rawget", // (table, index) -> value
    "rawget" to twoArgLuaFunction { arg1, arg2 ->
        when {
            arg1.isnil() -> BaseLib.argerror(1, "value")
            arg2.isnil() -> BaseLib.argerror(2, "value")
            else -> arg1.checktable().rawget(arg2)
        }
    },

    // "rawlen", // (v) -> value
    "rawlen" to oneArgLuaFunction { arg -> BaseLib.valueOf(arg.rawlen()) },

    // "rawset", // (table, index, value) -> table
    "rawset" to threeArgLuaFunction("rawset") { table, index, value ->
        when {
            index.isnil() -> BaseLib.argerror(2, "value")
            value.isnil() -> BaseLib.argerror(3, "value")
            else -> table.checktable().also { it.rawset(index.checknotnil(), value) }
        }
    },

    "next" to next,

    "pairs" to varargLuaFunction { args -> BaseLib.varargsOf(next, args.checktable(1), BaseLib.NIL) },

    "ipairs" to varargLuaFunction { args -> BaseLib.varargsOf(iNext, args.checktable(1), BaseLib.ZERO) },

    "select" to varargLuaFunction { args ->
        val n = args.narg() - 1
        if (args.arg1() == BaseLib.valueOf("#")) return@varargLuaFunction BaseLib.valueOf(n)
        val i = args.checkint(1).takeUnless { it == 0 || it < -n } ?: return@varargLuaFunction BaseLib.argerror(1, "index out of range")
        args.subargs(if (i < 0) n + i + 2 else i + 1)
    },

    "pcall" to LuaBox.luaFunctionProtectedCallWithoutDebugLib()
)

//class ProtectedCallEx(baseLib: RestrictedBaseLib) : VarArgFunction() {
//    private val globals: Globals = baseLib.globals
//
//    override fun invoke(args: Varargs): Varargs {
//        val t: LuaThread = globals.running
//        val prevError = t.errorfunc
//        t.errorfunc = args.checkvalue(2)
//        return try {
//            if (globals.debuglib != null) globals.debuglib.onCall(this)
//            try {
//                varargsOf(TRUE, args.arg1().invoke(args.subargs(3)))
//            } catch (e: LuaBoxEarlyExitException) {
//                throw e
//            } catch (le: LuaError) {
//                val cause = le.cause
//                if (cause != null && cause is LuaBoxEarlyExitException) throw cause
//
//                val m = le.messageObject
//                varargsOf(FALSE, m ?: NIL)
//            } catch (e: Exception) {
//                val m = e.message
//                varargsOf(FALSE, valueOf(m ?: e.toString()))
//            } finally {
//                if (globals.debuglib != null) globals.debuglib.onReturn()
//            }
//        } finally {
//            t.errorfunc = prevError
//        }
//    }
//}

//class ProtectedCall(baseLib: RestrictedBaseLib? = null) : VarArgFunction() {
//    private val globals: Globals? = baseLib?.globals
//
//    override fun invoke(args: Varargs): Varargs {
//        val func = args.checkvalue(1)
//        if (globals?.debuglib != null) globals.debuglib.onCall(this)
//        return try {
//            varargsOf(TRUE, func.invoke(args.subargs(2)))
//        } catch (e: LuaBoxEarlyExitException) {
//            throw e
//        } catch (le: LuaError) {
//            val cause = le.cause
//            if (cause != null && cause is LuaBoxEarlyExitException) throw cause
//
//            val m = le.messageObject
//            varargsOf(FALSE, m ?: NIL)
//        } catch (e: Exception) {
//            val m = e.message
//            varargsOf(FALSE, valueOf(m ?: e.toString()))
//        } finally {
//            if (globals?.debuglib != null) globals.debuglib.onReturn()
//        }
//    }
//}

private val S_VERSION = "${Lua._VERSION} lua5.2 LuaBoxKt".toLuaValue()

/**
 * Simple version of lua BaseLib
 */
@LuaBoxLib
fun LuaBox.Companion.luaLibBase() = varargLuaFunction {
    val environment = it.arg(2).checktable()
    environment.assertIsEnvironment()
    environment.putAll(mapOf("_G" to environment, "_VERSION" to S_VERSION,) + staticFunctions)
    environment
}

//class RestrictedBaseLib(
//    private val enableDebug: Boolean,
//    private val enableStandardPrint: Boolean
//) : BaseLib() {
//    /*
//     * Re-implementation of BaseLib
//     */
//
//    override fun loadStream(inputStream: InputStream?, chunkName: String, mode: String, env: LuaValue): Varargs {
//        return try {
//            if (inputStream == null) varargsOf(NIL, valueOf("not found: $chunkName"))
//            else globals.load(inputStream, chunkName, mode, env)
//        } catch (e: Exception) {
//            varargsOf(NIL, valueOf(e.message))
//        }
//    }
//
//    override fun loadFile(filename: String?, mode: String, env: LuaValue): Varargs {
//        return globals.finder.findResource(filename)?.use { inputStream ->
//            return loadStream(inputStream, "@$filename", mode, env)
//        } ?: varargsOf(NIL, valueOf("cannot open $filename: No such file or directory"))
//    }
//
//    override fun findResource(filename: String?): InputStream? {
//        return javaClass.getResourceAsStream(
//            if (filename!!.startsWith("/")) filename else "/$filename"
//        )
//    }
//
//}