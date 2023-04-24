/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*

private val PLATFORM_UTF16: String = if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) "UTF-16BE" else "UTF-16LE"

public actual object Charsets {
    public actual val UTF_8: Charset = NativeCharset("UTF-8")
    public actual val ISO_8859_1: Charset = NativeCharset("ISO-8859-1")
    internal val UTF_16: Charset = NativeCharset(PLATFORM_UTF16)
}

internal class NativeCharset(name: String) : Charset(name)
