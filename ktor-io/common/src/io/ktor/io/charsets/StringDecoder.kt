/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*

/**
 * Decodes String from bytes using specified [charset].
 */
public expect class StringDecoder {
    public val charset: Charset

    /**
     * Decodes as many characters as possible from [buffer] and return them as a String.
     */
    public fun decode(buffer: Buffer): String

    public fun flush(): String
}
