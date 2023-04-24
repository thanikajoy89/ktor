/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*

public actual fun String(
    bytes: ByteArray,
    offset: Int,
    length: Int,
    charset: Charset
): String = String(bytes, offset, length, charset.charset)


public actual fun String.toByteArray(
    charset: Charset
): ByteArray = toByteArray(charset.charset)
