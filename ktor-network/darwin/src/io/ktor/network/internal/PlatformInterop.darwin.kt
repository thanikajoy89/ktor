/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.internal

import kotlinx.cinterop.*
import platform.darwin.*
import kotlin.Byte

@OptIn(ExperimentalForeignApi::class)
internal actual fun inetNtopBridge(
    type: Int,
    address: CPointer<*>,
    addressOf: CPointer<*>,
    size: Int
) {
    @Suppress("UNCHECKED_CAST")
    inet_ntop(type, address, addressOf as CPointer<ByteVarOf<Byte>>, size.convert())
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun ktor_inet_ntop(
    family: Int,
    src: CValuesRef<*>?,
    dst: CValuesRef<ByteVar>?,
    size: Int
): CPointer<ByteVar>? = inet_ntop(family, src, dst, size.convert())
