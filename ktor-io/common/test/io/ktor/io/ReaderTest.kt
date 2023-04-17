/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.*
import kotlin.test.*

class ReaderTest {

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testCancelExceptionLoggedOnce() = testSuspend {
        var failInHandler = false
        val handler = CoroutineExceptionHandler { _, _ ->
            failInHandler = true
        }

        val reader = GlobalScope.reader(Dispatchers.Unconfined + handler) {
            error("Expected")
        }

        val cause = assertFailsWith<IllegalStateException> {
            reader.writeByte(42)
            reader.flush()
        }

        assertEquals("Expected", cause.message)
        assertTrue(failInHandler)
    }
}
