package com.seanshubin.json.console.domain

import java.lang.NumberFormatException

enum class Type {
    STRING {
        override fun fromString(s: String):Any = s
    },
    INT {
        override fun fromString(s: String): Any =
            try {
                Integer.parseInt(s)
            } catch(ex:NumberFormatException){
                s
            }
    };

    abstract fun fromString(s: String):Any
}
