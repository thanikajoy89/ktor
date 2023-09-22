/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.selector

import io.ktor.utils.io.core.*

internal expect class SignalPoint() : Closeable {
    fun wakeupDescriptor(): Long

    fun check()

    fun signal()
}
