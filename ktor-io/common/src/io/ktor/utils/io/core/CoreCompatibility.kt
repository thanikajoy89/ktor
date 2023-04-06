/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io.core

import io.ktor.io.*
import kotlin.math.*

@Deprecated(
    "Use buildPacket instead",
    ReplaceWith("buildPacket(block)", "io.ktor.io.buildPacket"),
    DeprecationLevel.WARNING
)
public fun buildPacket(block: Packet.() -> Unit): Packet = io.ktor.io.buildPacket(block)


@Deprecated(
    "Use ReadablePacket instead",
    ReplaceWith("ReadablePacket", "io.ktor.io.ReadablePacket"),
    DeprecationLevel.WARNING
)
public typealias ByteReadPacket = ReadablePacket

@Deprecated(
    "Use ReadablePacket instead",
    ReplaceWith("ReadablePacket", "io.ktor.io.ReadablePacket"),
    DeprecationLevel.WARNING
)
public typealias Input = ReadablePacket

@Deprecated(
    "Use availableForRead instead",
    ReplaceWith("availableForRead"),
    DeprecationLevel.WARNING
)
public val Packet.remaining: Int
    get() = availableForRead

@Deprecated(
    "Use availableForRead instead",
    ReplaceWith("availableForRead"),
    DeprecationLevel.WARNING
)
public val Packet.size: Int
    get() = availableForRead

@Deprecated(
    "Use writeString instead",
    ReplaceWith("writeString(value)"),
    DeprecationLevel.WARNING
)
public fun Packet.writeText(value: String): Unit {
    writeString(value)
}

@Deprecated(
    "Use toByteArray instead",
    ReplaceWith("toByteArray()")
)
public fun ReadablePacket.readBytes(): ByteArray = toByteArray()


@Deprecated(
    "Use toByteArray instead",
    ReplaceWith("toByteArray()")
)
public fun ReadablePacket.readBytes(size: Int): ByteArray = readByteArray(size)


public fun ReadablePacket.readAvailable(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int {
    val size = min(length, availableForRead)
    val result = readByteArray(size)
    result.copyInto(data, offset, 0, result.size)
    return size
}


