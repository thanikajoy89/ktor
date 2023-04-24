/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*
import io.ktor.io.Buffer
import java.nio.*

/**
 * Decodes String from bytes using specified [charset].
 */
public actual class StringDecoder(public actual val charset: Charset) {
    private val decoder = charset.newDecoder()
    private val outputBuffer = CharBuffer.allocate(4096)
    private val gapCache = ByteBuffer.allocate(32)

    /**
     * Decodes as many characters as possible from [buffer] and return them as a String.
     */
    public actual fun decode(buffer: Buffer): String {
        val content = buffer.readByteBuffer()
        if (gapCache.hasRemaining()) {

        }
        decoder.maxCharsPerByte()

        val decoded = decoder.decode(content, outputBuffer, false)
    }

    public actual fun flush(): String {
        TODO("Not yet implemented")
    }

}
