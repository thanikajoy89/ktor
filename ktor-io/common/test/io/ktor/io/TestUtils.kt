/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlin.test.*

fun assertArrayEquals(expected: ByteArray, actual: ByteArray) {
    assertEquals(expected.toList(), actual.toList())
}

fun ReadablePacket.incrementEachByte(): ReadablePacket = buildPacket {
    while (!this@incrementEachByte.isEmpty) {
        writeByte((this@incrementEachByte.readByte() + 1).toByte())
    }
}
