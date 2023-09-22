/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network

import platform.posix.*

internal actual fun createTempFilePath(basename: String): String = "/tmp/$basename"

internal actual fun removeFile(path: String) {
    if (remove(path) != 0) error("Failed to delete socket node")
}
