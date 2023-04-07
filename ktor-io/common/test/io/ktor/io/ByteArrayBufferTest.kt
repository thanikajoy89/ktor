/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.testing.*

class ByteArrayBufferTest : BufferTestBase() {
    override fun createBuffer(array: ByteArray): Buffer {
        return ByteArrayBuffer(array)
    }
}
