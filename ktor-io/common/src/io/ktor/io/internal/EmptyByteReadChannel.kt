/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.internal

import io.ktor.io.*
import kotlinx.coroutines.*

internal object EmptyByteReadChannel : ByteReadChannel {
    override val closedCause: Throwable? = null
    override val readablePacket: Packet = Packet()

    override fun isClosedForRead(): Boolean = true

    override suspend fun awaitBytesWhile(predicate: () -> Boolean) {}

    override fun cancel(cause: CancellationException?) {}
}
