package io.ktor.utils.io

import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 * A coroutine job that is reading from a byte channel
 */
@Deprecated(IO_DEPRECATION_MESSAGE)
public interface ReaderJob : Job {
    /**
     * A reference to the channel that this coroutine is reading from
     */
    public val channel: ByteWriteChannel
}

/**
 * A coroutine job that is writing to a byte channel
 */
@Deprecated(IO_DEPRECATION_MESSAGE)
public interface WriterJob : Job {
    /**
     * A reference to the channel that this coroutine is writing to
     */
    public val channel: ByteReadChannel
}

public interface ReaderScope : CoroutineScope {
    public val channel: ByteReadChannel
}

public interface WriterScope : CoroutineScope {
    public val channel: ByteWriteChannel
}

@Suppress("DEPRECATION")
@Deprecated(IO_DEPRECATION_MESSAGE)
public fun CoroutineScope.reader(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    channel: ByteChannel,
    block: suspend ReaderScope.() -> Unit
): ReaderJob = launchChannel(coroutineContext, channel, attachJob = false, block = block)

@Suppress("DEPRECATION")
@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
@Deprecated("Use scope.reader instead")
public fun reader(
    coroutineContext: CoroutineContext,
    channel: ByteChannel,
    parent: Job? = null,
    block: suspend ReaderScope.() -> Unit
): ReaderJob {
    val newContext = if (parent != null) {
        GlobalScope.newCoroutineContext(coroutineContext + parent)
    } else GlobalScope.newCoroutineContext(coroutineContext)

    return CoroutineScope(newContext).reader(EmptyCoroutineContext, channel, block)
}

@Suppress("DEPRECATION")
@Deprecated("Use scope.reader instead")
public fun reader(
    coroutineContext: CoroutineContext,
    autoFlush: Boolean = false,
    parent: Job? = null,
    block: suspend ReaderScope.() -> Unit
): ReaderJob {
    val channel = ByteChannel(autoFlush)
    return reader(coroutineContext, channel, parent, block).also {
        channel.attachJob(it)
    }
}

@Suppress("DEPRECATION")
@Deprecated(IO_DEPRECATION_MESSAGE)
public fun CoroutineScope.writer(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    channel: ByteChannel,
    block: suspend WriterScope.() -> Unit
): WriterJob = launchChannel(coroutineContext, channel, attachJob = false, block = block)

@Suppress("DEPRECATION")
@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
@Deprecated("Use scope.writer instead")
public fun writer(
    coroutineContext: CoroutineContext,
    channel: ByteChannel,
    parent: Job? = null,
    block: suspend WriterScope.() -> Unit
): WriterJob {
    val newContext = if (parent != null) {
        GlobalScope.newCoroutineContext(coroutineContext + parent)
    } else GlobalScope.newCoroutineContext(coroutineContext)

    return CoroutineScope(newContext).writer(EmptyCoroutineContext, channel, block)
}

@Suppress("DEPRECATION")
@Deprecated("Use scope.writer instead")
public fun writer(
    coroutineContext: CoroutineContext,
    autoFlush: Boolean = false,
    parent: Job? = null,
    block: suspend WriterScope.() -> Unit
): WriterJob {
    val channel = ByteChannel(autoFlush)
    return writer(coroutineContext, channel, parent, block).also {
        channel.attachJob(it)
    }
}

/**
 * @param S not exactly safe (unchecked cast is used) so should be [ReaderScope] or [WriterScope]
 */
@Suppress("DEPRECATION")
@OptIn(ExperimentalStdlibApi::class)
private fun <S : CoroutineScope> CoroutineScope.launchChannel(
    context: CoroutineContext,
    channel: ByteChannel,
    attachJob: Boolean,
    block: suspend S.() -> Unit
): ChannelJob {
    val dispatcher = coroutineContext[CoroutineDispatcher]
    val job = launch(context) {
        if (attachJob) {
            channel.attachJob(coroutineContext[Job]!!)
        }

        @Suppress("UNCHECKED_CAST")
        val scope = ChannelScope(this, channel) as S

        try {
            block(scope)
        } catch (cause: Throwable) {
            if (dispatcher != Dispatchers.Unconfined && dispatcher != null) {
                throw cause
            }

            channel.cancel(cause)
        } finally {
            channel.close()
        }
    }

    return ChannelJob(job, channel)
}

@Suppress("DEPRECATION")
private class ChannelScope(
    delegate: CoroutineScope,
    override val channel: ByteChannel
) : ReaderScope, WriterScope, CoroutineScope by delegate

@Suppress("DEPRECATION")
private class ChannelJob(
    private val delegate: Job,
    override val channel: ByteChannel
) : ReaderJob, WriterJob, Job by delegate {
    override fun toString(): String = "ChannelJob[$delegate]"
}
