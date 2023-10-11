/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

plugins {
    kotlin("plugin.serialization") version "1.9.20-Beta2"
    id("org.gradle.kotlin.kotlin-dsl") version "4.0.14"
}

val buildSnapshotTrain = properties["build_snapshot_train"]?.toString()?.toBoolean() == true

repositories {
    maven("https://plugins.gradle.org/m2")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")

    if (buildSnapshotTrain) {
        mavenLocal()
    }
}

sourceSets.main {
}

val ktor_version = "2.3.3"

var kotlin_version = "1.9.20-Beta2"
if (buildSnapshotTrain == true) {
    val kotlinSnapshotVersion = rootProject.properties["kotlin_snapshot_version"] as? String
    kotlin_version = kotlinSnapshotVersion ?: throw IllegalArgumentException(
        "'kotlin_snapshot_version' should be defined when building with snapshot compiler",
    )
}

dependencies {
    implementation(kotlin("gradle-plugin", kotlin_version))
    implementation(kotlin("serialization", kotlin_version))

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
