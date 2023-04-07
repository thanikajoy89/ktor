/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.jvm.javaio

import io.ktor.io.*
import kotlinx.coroutines.*
import java.io.*

internal class InputAdapter(parent: Job?, private val channel: ByteReadChannel) : InputStream() {
    private val context = Job()

    private val loop = object : BlockingAdapter(parent) {
        override suspend fun loop() {
            var readCount = 0
            while (true) {
                val buffer = rendezvous(readCount) as ByteArray
                if (channel.isEmpty) channel.awaitBytes()
                if (channel.isClosedForRead()) {
                    readCount = -1
                    context.complete()
                    break
                }

                val size = Integer.min(channel.availableForRead, buffer.size)
                val data = channel.readByteArray(size)
                data.copyInto(buffer, 0, 0, data.size)
                readCount = size
            }

            finish(readCount)
        }
    }

    private var single: ByteArray? = null

    override fun available(): Int {
        return channel.availableForRead
    }

    @Synchronized
    override fun read(): Int {
        val buffer = single ?: ByteArray(1).also { single = it }
        val rc = loop.submitAndAwait(buffer, 0, 1)
        if (rc == -1) return -1
        if (rc != 1) error("Expected a single byte or EOF. Got $rc bytes.")
        return buffer[0].toInt() and 0xff
    }

    @Synchronized
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return loop.submitAndAwait(b, off, len)
    }

    @Synchronized
    override fun close() {
        super.close()
        channel.cancel()
        if (!context.isCompleted) {
            context.cancel()
        }
        loop.shutdown()
    }
}
