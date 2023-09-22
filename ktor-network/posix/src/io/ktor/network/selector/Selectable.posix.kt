/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.network.selector

public actual interface Selectable {
    public val descriptor: Long
}

internal class SelectableNative(override val descriptor: Long) : Selectable {
    override fun toString(): String = "Selectable[$descriptor]"
}
