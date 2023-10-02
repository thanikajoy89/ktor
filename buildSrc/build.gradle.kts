/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

plugins {
    kotlin("plugin.serialization") version "1.8.20"
    id("org.gradle.kotlin.kotlin-dsl") version "4.0.14"
}

val buildSnapshotTrain = properties["build_snapshot_train"]?.toString()?.toBoolean() == true

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-atomicfu/maven").credentials {
        username = "margarita.bobova"
        password =
            "eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiIxcm1UZ20wbEFKaEoiLCJhdWQiOiJjaXJjbGV0LXdlYi11aSIsIm9yZ0RvbWFpbiI6InB1YmxpYyIsIm5hbWUiOiJtYXJnYXJpdGEuYm9ib3ZhIiwiaXNzIjoiaHR0cHM6XC9cL3B1YmxpYy5qZXRicmFpbnMuc3BhY2UiLCJwZXJtX3Rva2VuIjoiSVBwZlkwQ3M1cjUiLCJwcmluY2lwYWxfdHlwZSI6IlVTRVIiLCJpYXQiOjE2NDk5MjM2NDF9.olTvoKz6KSX1rMCkid3vCSvwy-95rQTYL9gVlj7ueudTEVGqXaq1tJc37FDnKL6i6oc26XLVDK0y4G_B7ZKJGoMh77nckx-XMmRxB4Q3LZY1cXo_Mt4zD9lPxfFAfHW9RboJFgNlLWzg3OVQvMwDgHetYhnuGmlTtzCKfCW3Ke4"
    }

    if (buildSnapshotTrain) {
        mavenLocal()
    }
}

sourceSets.main {
}

val ktor_version = "2.3.3"

configurations.configureEach {
    if (isCanBeResolved) {
        attributes {
            @Suppress("UnstableApiUsage")
            attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, project.objects.named(GradleVersion.current().version))
        }
    }
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.9.10"))
    implementation(kotlin("serialization", "1.9.10"))

    val ktlint_version = libs.versions.ktlint.version.get()
    implementation("org.jmailen.gradle:kotlinter-gradle:$ktlint_version")

    implementation("io.ktor:ktor-server-default-headers:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("io.ktor:ktor-server-jetty:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-caching-headers:$ktor_version")
    implementation("io.ktor:ktor-server-conditional-headers:$ktor_version")
    implementation("io.ktor:ktor-server-compression:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx:$ktor_version")
    implementation("io.ktor:ktor-network-tls-certificates:$ktor_version")

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback.classic)
}

kotlin {
    jvmToolchain {
        check(this is JavaToolchainSpec)
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xsuppress-version-warnings",
        "-Xskip-metadata-version-check",
    )
}
