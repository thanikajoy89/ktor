/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.util

import io.ktor.io.*
import io.ktor.io.copyToBoth
import io.ktor.io.split
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*

private const val CHUNK_BUFFER_SIZE = 4096L

/**
 * Split source [ByteReadChannel] into 2 new one.
 * Cancel of one channel in split(input or both outputs) cancels other channels.
 */
@Deprecated(
    "Use io.ktor.io instead", level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith("io.ktor.io.ByteReadChannel.split(coroutineScope)")
)
public fun ByteReadChannel.split(coroutineScope: CoroutineScope): Pair<ByteReadChannel, ByteReadChannel> =
    split(coroutineScope)

/**
 * Copy source channel to both output channels chunk by chunk.
 */
public fun ByteReadChannel.copyToBoth(first: ByteWriteChannel, second: ByteWriteChannel) {
    GlobalScope.launch {
        copyToBoth(first, second)
    }
}

/**
 * Read channel to byte array.
 */
public suspend fun ByteReadChannel.toByteArray(): ByteArray = readRemaining().readBytes()
