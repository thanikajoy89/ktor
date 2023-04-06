/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io.charsets

import io.ktor.io.toByteArray

@Deprecated(
    "Use Charsets instead",
    ReplaceWith("Charsets", "io.ktor.io.charsets.Charsets"),
    DeprecationLevel.WARNING
)
public typealias Charsets = io.ktor.io.charsets.Charsets

@Deprecated(
    "Use Charset instead",
    ReplaceWith("Charsets", "io.ktor.io.charsets.Charsets"),
    DeprecationLevel.WARNING
)
public typealias Charset = io.ktor.io.charsets.Charset

@Deprecated(
    "Use String instead",
    ReplaceWith("Charset", "io.ktor.io.String"),
    DeprecationLevel.WARNING
)
public fun String(
    bytes: ByteArray,
    offset: Int = 0,
    length: Int = bytes.size - offset,
    charset: Charset = io.ktor.io.charsets.Charsets.UTF_8
): String = io.ktor.io.String(bytes, offset, length, charset)

@Deprecated(
    "Use io.ktor.io instead",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith("io.ktor.io.toByteArray()")
)
public fun String.toByteArray(charset: Charset): ByteArray = toByteArray(Charsets.UTF_8)
