/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

public actual object Charsets {
    public actual val UTF_8: Charset = JsCharset("UTF-8")
    public actual val ISO_8859_1: Charset = JsCharset("ISO-8859-1")
}

internal class JsCharset(name: String) : Charset(name)

