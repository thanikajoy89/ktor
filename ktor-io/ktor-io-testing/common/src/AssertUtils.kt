/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.testing

import kotlin.test.*

public fun assertArrayEquals(expect: ByteArray, actual: ByteArray) {
    assertEquals(expect.toList(), actual.toList())
}
