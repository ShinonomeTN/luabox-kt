# LuaBox Core Library

LuaBox core library provide essential functions to run lua codes. 
It provides **no standard functions**(such as `string`, `table`) but only the language runtime.

## Basic usage

### First, create a Lua language runtime template

```kotlin
val luabox = LuaBox.default()
```

`LuaBox.default()` creates a default lua language runtime, allowing lua scripts and bytecode contents to be loaded and execute.

You can create a runtime without bytecode loader using `LuaBox.withoutByteCodeLoader()`

Or using `LuaBox.customized {}` DSL to create a customized runtime. Currently, it supports those customization:

```kotlin
val luabox = LuaBox.customized {
    unDumper = null // a Lua's Global.Undumper to load lua bytecodes
    compiler = LuaC.instance // Lua compiler
    loader = LuaC.instance // Lua loader
}
```

For details please see the source codes of Lua4J

### Second, load lua script as a closure (or say, function)

```kotlin
val closure = luabox.load("return 1 + 2", createLuaEnvironment())
```

You need to provide a `LuaBoxEnvironment` as Lua Global to compile/load lua scripts/bytecodes.
A `LuaBoxEnvironment` can be created by calling `createLuaEnvironment()`. It is a standard `LuaTable`.
`createLuaEnvironment()` also supporting customization, library loading and pre-configurations for the Lua Global require this.

```kotlin
val global = createLuaEnvironment {
    // Your customization goes here
}
```

Details of customization DSLs will be mentions later.

### Last step, calling the lua closure

```kotlin
val result = closure.call()
println(result)
```

