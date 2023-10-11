/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.js.node

import io.ktor.client.engine.js.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.khronos.webgl.*
import org.w3c.fetch.*
import io.ktor.client.utils.*

internal external interface ResponseBody : JsAny {
    fun pause(): Unit
    fun resume(): Unit
    fun destroy(cause: JsAny): Unit
}

private fun <T: JsAny> bodyOn(body: ResponseBody, type: String, handler: (T) -> Unit): Unit =
    js("body.on(type, handler)")

private fun bodyOn(body: ResponseBody, type: String, handler: () -> Unit): Unit =
    js("body.on(type, handler)")

internal fun CoroutineScope.readBodyNode(response: Response): ByteReadChannel = writer {
    val body = response.body?.unsafeCast<ResponseBody>() ?: error("Fail to get body")

    val responseData = Channel<ByteArray>(1)

    bodyOn(body, "data") { buffer: JsAny ->
        responseData.trySend(Uint8Array(buffer.unsafeCast<ArrayBuffer>()).asByteArray()).isSuccess
        body.pause()
    }

    bodyOn<JsAny>(body, "error") { error: JsAny ->
        val cause = JsError(error)
        responseData.close(cause)
        channel.close(cause)
    }

    bodyOn(body, "end") {
        responseData.close()
    }

    try {
        for (chunk in responseData) {
            channel.writeFully(chunk)
            channel.flush()
            body.resume()
        }
    } catch (cause: Throwable) {
        body.destroy(cause.toJsReference())
        throw cause
    }
}.channel
