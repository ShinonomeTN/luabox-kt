package com.shinonometn.kt.luabox

import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue

/** Throws a lua error with message */
@LuaBoxDsl
fun luaError(message: String): Nothing = throw LuaError(message)

/** Throws a lua error with nested exception */
@LuaBoxDsl
fun luaError(throwable: Throwable) : Nothing = throw LuaError(throwable)

/** Throws a lua invalid argument error */
@LuaBoxDsl
fun luaArgError(index : Int, message : String): Nothing {
    LuaValue.argerror(index, message)
    throw IllegalStateException()
}

/** This exception is for exiting the lua closure execution.
 * When this exception is throws means the script requests an exit operation.
 * Can provide an optional [exitCode]. */
class LuaBoxEarlyExitException(val exitCode : Int) : InterruptedException(exitCode.toString())

/** Exit the script execution, with an exit code. */
fun luaExit(exitCode: Int) : Nothing = throw LuaBoxEarlyExitException(exitCode)