/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 * Factory interface for creating [ApplicationEngine] instances
 */
public interface ApplicationEngineFactory<
    out TEngine : ApplicationEngine,
    TConfiguration : ApplicationEngine.Configuration
    > {
    /**
     * Creates an engine from the given [environment] and [configure] script
     */
    public fun create(environment: ApplicationEngineEnvironment, configure: TConfiguration.() -> Unit): TEngine
}

/**
 * Creates a Ktor HTTP server.
 *
 * This function configure and build HTTP server and start it on the specific [port].
 *
 * The simple http server can be configured and started as follows:
 * ```kotlin
 * val server = embeddedServer(Netty, port = 9090) {
 *     routing {
 *         get("/") {
 *             call.respondText("Hello, world!")
 *         }
 *     }
 * }
 *
 * server.start(wait = true)
 * ```
 *
 * You can learn more about creating new Ktor project in the documentation: https://ktor.io/docs/create-server.html#engine-main
 *
 * @param factory is an object referring to the application engine like [Netty], [CIO], [Jetty]. To learn more about
 * engine check the documentation: https://ktor.io/docs/engines.html
 *
 * @param watchPaths specifies a list of paths that will be watched for automatic reloading. You can learn more about
 * auto-reloading in the documentation: https://ktor.io/docs/auto-reload.html#watch-paths
 *
 * @param configure configuration script for the engine
 * @param module application module function
 */
@OptIn(DelicateCoroutinesApi::class)
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>
embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    port: Int = 80,
    host: String = "0.0.0.0",
    watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
    configure: TConfiguration.() -> Unit = {},
    module: Application.() -> Unit
): TEngine = GlobalScope.embeddedServer(factory, port, host, watchPaths, EmptyCoroutineContext, configure, module)

/**
 * Creates a Ktor server.
 *
 * The simple http server can be configured and started as follows:
 * ```kotlin
 * val server = embeddedServer(Netty, port = 9090) {
 *     routing {
 *         get("/") {
 *             call.respondText("Hello, world!")
 *         }
 *     }
 * }
 *
 * server.start(wait = true)
 * ```
 *
 * You can learn more about creating new Ktor project in the documentation: https://ktor.io/docs/create-server.html#engine-main
 *
 * @param factory is an object referring to the application engine like [Netty], [CIO], [Jetty]. To learn more about
 * engine check the documentation: https://ktor.io/docs/engines.html
 *
 * @param watchPaths specifies a list of paths that will be watched for automatic reloading. You can learn more about
 * auto-reloading in the documentation: https://ktor.io/docs/auto-reload.html#watch-paths
 *
 * @param configure configuration script for the engine
 * @param parentCoroutineContext specifies a coroutine context to be used for server jobs
 * @param module application module function
 */
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>
    CoroutineScope.embeddedServer(
        factory: ApplicationEngineFactory<TEngine, TConfiguration>,
        port: Int = 80,
        host: String = "0.0.0.0",
        watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
        configure: TConfiguration.() -> Unit = {},
        module: Application.() -> Unit
    ): TEngine {
    val connectors: Array<EngineConnectorConfig> = arrayOf(
        EngineConnectorBuilder().apply {
            this.port = port
            this.host = host
        }
    )
    return embeddedServer(
        factory = factory,
        connectors = connectors,
        watchPaths = watchPaths,
        parentCoroutineContext = parentCoroutineContext,
        configure = configure,
        module = module
    )
}

/**
 * Creates a Ktor server.
 *
 * The simple http server can be configured and started as follows:
 * ```kotlin
 * val server = embeddedServer(Netty, port = 9090) {
 *     routing {
 *         get("/") {
 *             call.respondText("Hello, world!")
 *         }
 *     }
 * }
 *
 * server.start(wait = true)
 * ```
 *
 * You can learn more about creating new Ktor project in the documentation: https://ktor.io/docs/create-server.html#engine-main
 *
 * @param factory is an object referring to the application engine like [Netty], [CIO], [Jetty]. To learn more about
 * engine check the documentation: https://ktor.io/docs/engines.html
 *
 * @param connectors default listening on 0.0.0.0:80
 *
 * @param watchPaths specifies a list of paths that will be watched for automatic reloading. You can learn more about
 * auto-reloading in the documentation: https://ktor.io/docs/auto-reload.html#watch-paths
 *
 * @param parentCoroutineContext specifies a coroutine context to be used for server jobs
 * @param configure configuration script for the engine
 * @param module application module function
 */
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>
    CoroutineScope.embeddedServer(
        factory: ApplicationEngineFactory<TEngine, TConfiguration>,
        vararg connectors: EngineConnectorConfig = arrayOf(EngineConnectorBuilder()),
        watchPaths: List<String> = listOf(WORKING_DIRECTORY_PATH),
        parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
        configure: TConfiguration.() -> Unit = {},
        module: Application.() -> Unit
    ): TEngine {
    val environment = applicationEngineEnvironment {
        this.parentCoroutineContext = coroutineContext + parentCoroutineContext
        this.log = KtorSimpleLogger("io.ktor.server.Application")
        this.watchPaths = watchPaths
        this.module(module)
        this.connectors.addAll(connectors)
    }

    return embeddedServer(factory, environment, configure)
}

/**
 * Creates a Ktor server.
 *
 * The simple http server can be configured and started as follows:
 * ```kotlin
 * val server = embeddedServer(Netty, port = 9090) {
 *     routing {
 *         get("/") {
 *             call.respondText("Hello, world!")
 *         }
 *     }
 * }
 *
 * server.start(wait = true)
 * ```
 *
 * You can learn more about creating new Ktor project in the documentation: https://ktor.io/docs/create-server.html#engine-main
 *
 * @param factory is an object referring to the application engine like [Netty], [CIO], [Jetty]. To learn more about
 * engine check the documentation: https://ktor.io/docs/engines.html
 */
public fun <TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration> embeddedServer(
    factory: ApplicationEngineFactory<TEngine, TConfiguration>,
    environment: ApplicationEngineEnvironment,
    configure: TConfiguration.() -> Unit = {}
): TEngine {
    return factory.create(environment, configure)
}
