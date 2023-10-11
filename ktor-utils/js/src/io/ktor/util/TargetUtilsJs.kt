/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util

import org.khronos.webgl.Int8Array
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise
import kotlinx.coroutines.asDeferred

public actual fun ByteArray.toJsArray(): Int8Array = this.unsafeCast<Int8Array>()
public actual fun Int8Array.toByteArray(): ByteArray = this.unsafeCast<ByteArray>()

internal actual suspend fun Promise<ArrayBuffer>.awaitBuffer(): ArrayBuffer =
    asDeferred<ArrayBuffer>().await()
