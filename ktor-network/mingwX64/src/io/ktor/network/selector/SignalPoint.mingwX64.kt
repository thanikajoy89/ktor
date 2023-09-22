/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.selector

import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlinx.atomicfu.*
import kotlinx.cinterop.*
import platform.posix.*

internal actual class SignalPoint actual constructor() : Closeable {
    private val current = atomic<Long?>(null)

    @OptIn(ExperimentalForeignApi::class)
    actual fun wakeupDescriptor(): Long {
        val old = current.value
        if (old != null) return old

        val result = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)
        if (result == INVALID_SOCKET) {
            throw PosixException.forErrno()
        }

        current.value = result.toLong()
        return result.convert()
    }

    actual fun check() {}

    @OptIn(ExperimentalForeignApi::class)
    actual fun signal() {
        val current = current.getAndSet(null)
        if (current != null) {
            close(current.convert())
        }
    }

    override fun close() {
        signal()
    }
}
