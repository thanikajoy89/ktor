/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlin.test.*

class OutputStreamAdapterTest {
    @Test
    fun testWriteMessage() = runBlocking {
        val message = "Hello, world!"

        val result = CompletableDeferred<String>()
        reader {
            consume {
                result.complete(it.readString())
            }
        }.toOutputStream().writer().use {
            it.write(message)
        }

        assertEquals(message, result.await())
    }

    @Test
    fun testFlush(): Unit = runBlocking {
        val first = CompletableDeferred<Int>()
        val channel = reader {
            consume {
                it.readByte()
                first.complete(42)
            }
        }

        val stream = channel.toOutputStream()
        stream.write(42)
        assertTrue(!first.isCompleted)
        stream.flush()

        assertEquals(42, first.await())
        stream.close()
    }
}
