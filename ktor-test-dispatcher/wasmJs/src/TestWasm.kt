/*
* Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.test.dispatcher

import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 * Test runner for js suspend tests.
 */
@OptIn(DelicateCoroutinesApi::class)
public actual fun testSuspend(
    context: CoroutineContext,
    timeoutMillis: Long,
    block: suspend CoroutineScope.() -> Unit
) {
    GlobalScope.promise(block = {
        withTimeout(timeoutMillis, block)
        null
    }, context = context)
}
