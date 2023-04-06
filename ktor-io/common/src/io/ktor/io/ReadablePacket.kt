/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*

public val ReadablePacket.isEmpty: Boolean get() = availableForRead == 0

public val ReadablePacket.isNotEmpty: Boolean get() = availableForRead > 0

public interface ReadablePacket : Closeable {
    public val availableForRead: Int

    public fun peek(): ReadableBuffer
    public fun readBuffer(): ReadableBuffer
    public fun readBuffers(): List<ReadableBuffer>

    public fun readByte(): Byte
    public fun readShort(): Short
    public fun readInt(): Int
    public fun readLong(): Long
    public fun readDouble(): Double
    public fun readFloat(): Float

    public fun indexOf(buffer: ReadableBuffer): Int
    public fun discard(limit: Int): Int
    public fun discardExact(count: Int): Int

    public fun toByteArray(): ByteArray
    public fun readByteArray(length: Int): ByteArray

    public fun readString(charset: Charset = Charsets.UTF_8): String
    public fun readPacket(length: Int): Packet

    public fun steal(): Packet
    public fun clone(): Packet
}
