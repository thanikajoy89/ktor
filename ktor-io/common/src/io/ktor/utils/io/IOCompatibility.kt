/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.io.*

import io.ktor.io.readRemaining

@Deprecated(
    "Use io.ktor.io instead", ReplaceWith("ByteReadChanel", "io.ktor.io.ByteReadChannel")
)
public typealias ByteReadChannel = io.ktor.io.ByteReadChannel

@Deprecated(
    "Use io.ktor.io instead", ReplaceWith("ByteWriteChannel", "io.ktor.io.ByteWriteChannel")
)
public typealias ByteWriteChannel = io.ktor.io.ByteWriteChannel

@Deprecated(
    "Use io.ktor.io package instead",
    ReplaceWith(
        "io.ktor.io.readRemaining",
        "io.ktor.io.readRemaining"
    )
)
public suspend fun ByteReadChannel.readRemaining(limit: Long = Long.MAX_VALUE): ReadablePacket = readRemaining(limit)
