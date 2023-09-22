/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network

internal actual fun Any.supportsUnixDomainSockets(): Boolean = false

internal actual fun createTempFilePath(basename: String): String {
    TODO()
}

internal actual fun removeFile(path: String) {
    TODO()
}

