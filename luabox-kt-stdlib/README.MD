# LuaBox Standard Library

## BaseLib

### Global Variables

#### Standard functions

LuaBox's BaseLib has those standard functions preloaded:

- assert
- error
- type
- tonumber
- tostring
- getmetatable
- setmetatable
- rawequal
- rawget
- rawlen
- rawset
- next
- pairs
- ipairs
- select
- pcall

They are all reimplemented in Kotlin

#### _VERSION : LuaBox version

`_VERSION` can be changed by those variables:

- System property `com.shinonometn.luabox.version_string`
- Environment variable `LUABOX_KT_VERSION_STRING`

The default value is `"${Lua._VERSION} lua5.2 LuaBoxKt"`