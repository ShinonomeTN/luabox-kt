# LuaBox Kt

Lua runtime on JVM written in Kotlin. Based on LuaJ.

## Usage

```kotlin
val luabox = LuaBox.default()
val result = luabox.load("return 1 + 2", createLuaEnvironment()).call()
println(result.toString())
```

Because LuaJ is such ancient that I need to rewrite many things to make things work in modern style.

Still in progress, also documents.