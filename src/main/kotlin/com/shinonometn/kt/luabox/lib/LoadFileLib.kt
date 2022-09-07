package com.shinonometn.kt.luabox.lib

import com.shinonometn.kt.luabox.LuaBox
import com.shinonometn.kt.luabox.LuaBoxPackage
import com.shinonometn.kt.luabox.luaBoxPackage
import com.shinonometn.kt.luabox.varargLuaFunction
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs


//class LoadFileLib(private val luaBox : LuaBox) : LuaBoxPackage("loadfile") {
//    override fun providePackage(globals: Globals): LuaValue {
//        globals["dofile"] = varargLuaFunction { args ->
//            args.argcheck(args.isstring(1) || args.isnil(1), 1, "filename must be string or nil")
//
//            val v: Varargs = when (if (args.isstring(1)) args.tojstring(1) else null) {
//                null -> luaBox.load(globals.STDIN, "=stdin", "bt", globals)
//                else -> baseLib.loadFile(args.checkjstring(1), "bt", globals)
//            }
//
//            if (v.isnil(1)) error(v.tojstring(2)) else v.arg1().invoke()
//        }
//
//        globals["load"] = varargLuaFunction { args ->
//            val ld = args.arg1()
//            args.argcheck(ld.isstring() || ld.isfunction(), 1, "ld must be string or function")
//            val source = args.optjstring(2, if (ld.isstring()) ld.tojstring() else "=(load)")
//            val mode = args.optjstring(3, "bt")
//            val env = args.optvalue(4, globals)
//            baseLib.loadStream(
//                if (ld.isstring()) ld.strvalue().toInputStream()
//                else LuaStringInputStream(ld.checkfunction()), source, mode, env
//            )
//        }
//
//        return varargLuaFunction { args ->
//            args.argcheck(args.isstring(1) || args.isnil(1), 1, "filename must be string or nil")
//            val filename = if (args.isstring(1)) args.tojstring(1) else null
//            val mode = args.optjstring(2, "bt")
//            val env = args.optvalue(3, globals)
//            filename?.let { baseLib.loadFile(it, mode, env) } ?: baseLib.loadStream(globals.STDIN, "=stdin", mode, env)
//        }
//    }
//}