/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import platform.iconv.*

public actual abstract class Charset(internal val name: String) {
    init {
        val v = iconv_open(name, "UTF-8")
        checkErrors(v, name)
        iconv_close(v)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Charset) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }

    public actual fun name(): String = name

//    public actual companion object {
//        public actual fun forName(name: String): Charset {
//            if (name == "UTF-8" || name == "utf-8" || name == "UTF8" || name == "utf8") return Charsets.UTF_8
//            if (name == "ISO-8859-1" || name == "iso-8859-1" || name == "ISO_8859_1") return Charsets.ISO_8859_1
//            if (name == "UTF-16" || name == "utf-16" || name == "UTF16" || name == "utf16") return Charsets.UTF_16
//
//            return Charset(name)
//        }
//
//        public actual fun isSupported(charset: String): Boolean = when (charset) {
//            "UTF-8", "utf-8", "UTF8", "utf8" -> true
//            "ISO-8859-1", "iso-8859-1" -> true
//            "UTF-16", "utf-16", "UTF16", "utf16" -> true
//            else -> false
//        }
//    }
}

