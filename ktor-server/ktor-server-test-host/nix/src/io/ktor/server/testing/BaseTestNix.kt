/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.testing

import io.ktor.test.dispatcher.*
import io.ktor.utils.io.*
import io.ktor.utils.io.locks.*
import kotlinx.coroutines.*
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

@Suppress("FunctionName")
@AllowDifferentMembersInActual
actual abstract class BaseTest actual constructor() {
    actual open val timeout: Duration = 10.seconds

    private val errors = mutableListOf<Throwable>()

    @OptIn(InternalAPI::class)
    private val errorsLock = SynchronizedObject()

    @OptIn(InternalAPI::class)
    actual fun collectUnhandledException(error: Throwable) {
        synchronized(errorsLock) {
            errors.add(error)
        }
    }

    @AfterTest
    fun _verifyErrors() {
        if (errors.isEmpty()) return

        val error = UnhandledErrorsException(
            "There were ${errors.size} unhandled errors during running test (suppressed)"
        )

        errors.forEach {
            error.addSuppressed(it)
        }
        error.printStackTrace()
        throw error // suppressed exceptions print wrong in idea
    }

    actual fun runTest(block: suspend CoroutineScope.() -> Unit) {
        testSuspend(timeoutMillis = timeout.inWholeMilliseconds, block = block)
    }
}

private class UnhandledErrorsException(override val message: String) : Exception()
private class TestTimeoutException(override val message: String, override val cause: Throwable?) : Exception()
