/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.network.sockets

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.util.*
import io.ktor.utils.io.*
import java.net.*

/**
 * This exception is thrown in case connect timeout exceeded.
 */
@Suppress("ACTUAL_WITHOUT_EXPECT")
public actual class ConnectTimeoutException actual constructor(
    message: String,
    override val cause: Throwable?
) : ConnectException(message)

/**
 * This exception is thrown in case socket timeout (read or write) exceeded.
 */
@Suppress("ACTUAL_WITHOUT_EXPECT")
public actual class SocketTimeoutException actual constructor(
    message: String,
    override val cause: Throwable?
) : java.net.SocketTimeoutException(message)

@OptIn(InternalAPI::class)
internal actual val timeoutExceptionMapper: (HttpRequestData) -> ((Throwable?) -> Throwable?) = { request ->
    { cause ->
        when (cause?.rootCause) {
            is java.net.SocketTimeoutException -> SocketTimeoutException(request, cause)
            else -> cause
        }
    }
}
