package com.shinonometn.kt.luabox

@DslMarker
@Target(AnnotationTarget.FUNCTION)
annotation class LuaBoxLib

@DslMarker
annotation class LuaBoxDsl

@DslMarker
@Target(AnnotationTarget.FUNCTION)
annotation class LuaBoxFunc