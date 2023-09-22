/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.selector

import kotlinx.coroutines.*
import kotlin.coroutines.*

internal class SelectorManagerJs : SelectorManager {
    override val coroutineContext: CoroutineContext = Job()

    override fun close() {
    }

    override fun notifyClosed(selectable: Selectable) {
    }

    override suspend fun select(selectable: Selectable, interest: SelectInterest) {
    }
}
