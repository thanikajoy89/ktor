/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.testing

import io.ktor.io.*
import kotlin.test.*

public abstract class BufferTestBase {

    protected abstract fun createBuffer(array: ByteArray): Buffer

    @Test
    public fun testEmptyToByteArray() {
        val buffer = createBuffer(ByteArray(0))
        assertTrue {
            ByteArray(0).contentEquals(buffer.toByteArray())
        }
    }

    @Test
    public fun testReadBuffer() {
        val buffer = createBuffer("Hello".toByteArray())

        for (char in "Hello") {
            assertEquals(char.code.toByte(), buffer.readBuffer(1).readByte())
        }
    }

    @Test
    public fun testReadStringFromEmpty() {
        val buffer = createBuffer(ByteArray(0))
        assertFailsWith<IndexOutOfBoundsException> {
            buffer.readString()
        }
    }

    @Test
    public fun testReadByteArrayFromEmpty() {
        val buffer = createBuffer(ByteArray(0))
        assertFailsWith<IllegalArgumentException> {
            buffer.readByteArray(1)
        }

        assertEquals(0, buffer.readByteArray(0).size)
    }

    @Test
    public fun testIllegalIndexes() {
        val buffer = createBuffer(ByteArray(0))

        assertFailsWith<IllegalArgumentException> {
            buffer.readIndex = -1
        }
        assertFailsWith<IllegalArgumentException> {
            buffer.readIndex = 1
        }
        assertFailsWith<IllegalArgumentException> {
            buffer.writeIndex = -1
        }
        assertFailsWith<IllegalArgumentException> {
            buffer.writeIndex = 1
        }
    }

    @Test
    public fun testIndexOfPrefix() {
        val buffer = createBuffer(byteArrayOf(1, 2, 3, 4, 5))

        assertEquals(0, buffer.indexOfPrefix(createBuffer(byteArrayOf(1, 2, 3))))
        assertEquals(0, buffer.indexOfPrefix(createBuffer(byteArrayOf(1))))
        assertEquals(0, buffer.indexOfPrefix(createBuffer(byteArrayOf())))

        assertEquals(1, buffer.indexOfPrefix(createBuffer(byteArrayOf(2, 3))))
        assertEquals(1, buffer.indexOfPrefix(createBuffer(byteArrayOf(2))))

        assertEquals(4, buffer.indexOfPrefix(createBuffer(byteArrayOf(5))))
    }

    @Test
    public fun testIndexOfPrefixNotFound() {
        val buffer = createBuffer(byteArrayOf(1, 2, 3, 4, 5))

        assertEquals(-1, buffer.indexOfPrefix(createBuffer(byteArrayOf(2, 3, 5))))
        assertEquals(-1, buffer.indexOfPrefix(createBuffer(byteArrayOf(2, 3, 1))))
        assertEquals(-1, buffer.indexOfPrefix(createBuffer(byteArrayOf(3, 1))))
        assertEquals(-1, buffer.indexOfPrefix(createBuffer(byteArrayOf(6))))
    }

    @Test
    public fun testIndexOfPrefixInTail() {
        val buffer = createBuffer(byteArrayOf(1, 2, 3, 4, 5))

        assertEquals(0, buffer.indexOfPrefix(createBuffer(byteArrayOf(1, 2, 3, 4, 5, 6))))
        assertEquals(-1, createBuffer(ByteArray(0)).indexOfPrefix(createBuffer(ByteArray(0))))
    }

    @Test
    public fun testCommonPrefixLengthForEmpty() {
        val empty = ByteArray(0)
        val single = createBuffer(ByteArray(1) { 42 })
        assertEquals(0, createBuffer(empty).commonPrefixLength(createBuffer(empty)))

        assertEquals(0, single.commonPrefixLength(createBuffer(empty)))
        assertEquals(0, createBuffer(empty).commonPrefixLength(single))
    }

    @Test
    public fun testCommonPrefixLengthZero() {
        assertEquals(0, createBuffer(byteArrayOf(1, 2, 3)).commonPrefixLength(createBuffer(byteArrayOf(4, 5, 6))))
    }

    @Test
    public fun testCommonPrefixLength() {
        val single = createBuffer(ByteArray(1) { 42 })
        assertEquals(1, single.commonPrefixLength(single))

        val first = createBuffer(byteArrayOf(1, 2, 3, 4, 5))
        val second = createBuffer(byteArrayOf(1, 2, 3, 4))

        assertEquals(4, first.commonPrefixLength(second))
        assertEquals(4, second.commonPrefixLength(first))
    }

    @Test
    public fun testWriteToClone() {
        val buffer = createBuffer(byteArrayOf(1, 2, 3, 4, 5))
        val clone = buffer.clone()
        buffer[0] = 10
        clone[0] = 20
        clone[1] = 30
        buffer[1] = 40

        assertArrayEquals(byteArrayOf(10, 40, 3, 4, 5), buffer.toByteArray())
        assertArrayEquals(byteArrayOf(20, 30, 3, 4, 5), clone.toByteArray())
    }
}
