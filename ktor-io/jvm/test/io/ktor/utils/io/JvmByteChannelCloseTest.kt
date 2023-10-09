/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import kotlinx.coroutines.channels.*

class JvmReadPacketWithExceptionByteChannelCloseTest : ByteChannelCloseTest(
    ClosedReceiveChannelException::class,
    { close() },
    { readPacket(Int.MAX_VALUE) }
)

class JvmReadFullyWithExceptionByteChannelCloseTest : ByteChannelCloseTest(
    ClosedReceiveChannelException::class,
    { close() },
    { readFully(ByteArray(10)) }
)

