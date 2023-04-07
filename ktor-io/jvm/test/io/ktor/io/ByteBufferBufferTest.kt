/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.testing.*
import java.nio.*

class ByteBufferBufferTest : BufferTestBase() {
    override fun createBuffer(array: ByteArray): Buffer {
        return ByteBufferBuffer(ByteBuffer.wrap(array))
    }
}
