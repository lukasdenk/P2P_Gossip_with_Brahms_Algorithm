package networking.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import networking.service.Constants
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * @param write Writes first message as soon as connection established
 */
@ExperimentalTime
@Suppress("BlockingMethodInNonBlockingContext")
class OneWayMessageClient(
    address: String,
    port: Int,
    private val write: ((ByteArray) -> Unit) -> Unit
) {

    private val socketScope = CoroutineScope(Dispatchers.IO)
    private val address: SocketAddress = InetSocketAddress(address, port)
    private val reconnectedTimes = AtomicInteger(0)
    private val isUp = AtomicBoolean(true)

    suspend fun start() {
        connect()
        while (isUp.get()) {
            delay(Duration.seconds(10))
        }
    }

    private fun connect() {
        isUp.set(true)
        socketScope.launch {
            val socketChannel = AsynchronousSocketChannel.open()
            socketChannel.connect(address, socketChannel, ConnectionHandler(
                write,
                connectionClosed = {
                    isUp.set(false)
                },
                connectionFailed = { reconnect() }
            ))
        }
    }

    private fun reconnect() {
        if (reconnectedTimes.addAndGet(1) > Constants.MaxReconnectAttempts) {
            println("[${this::class.simpleName}] Was not able to connect to the gossip service.")
            isUp.set(false)
            return
        }
        println("[${this::class.simpleName}] Attempt #${reconnectedTimes.get()} to reconnect after ${Constants.ReconnectionIntervalInSec} sec")
        socketScope.launch {
            delay(Duration.seconds(Constants.ReconnectionIntervalInSec))
            connect()
        }
    }

    private class ConnectionHandler(
        private val write: ((ByteArray) -> Unit) -> Unit,
        private val connectionClosed: () -> Unit = {},
        private val connectionFailed: () -> Unit = {}
    ) : CompletionHandler<Void, AsynchronousSocketChannel> {

        private lateinit var socketChannel: AsynchronousSocketChannel

        override fun completed(result: Void?, socketChannel: AsynchronousSocketChannel) {
            this.socketChannel = socketChannel
            sendFirstMessage()
        }

        private fun sendFirstMessage() {
            if (!socketChannel.isOpen) {
                closeChannel()
                return
            }
            write.invoke { bytes: ByteArray ->
                write(bytes)
            }
        }

        private fun write(bytes: ByteArray) {
            socketChannel.write(
                ByteBuffer.wrap(bytes),
                bytes,
                WriteHandler(
                    writeCompleted = {
                        closeChannel()
                    }
                )
            )
        }

        private fun closeChannel() {
            socketChannel.close()
            connectionClosed.invoke()
        }

        override fun failed(exc: Throwable?, socketChannel: AsynchronousSocketChannel) {
            println("[${this::class.simpleName}] failed to connect to $exc")
            connectionFailed.invoke()
        }

    }

    private class WriteHandler(
        private val writeCompleted: () -> Unit = {},
        private val writeFailed: () -> Unit = {}
    ) : CompletionHandler<Int, ByteArray> {

        override fun completed(result: Int, attachment: ByteArray) {
            writeCompleted.invoke()
        }

        override fun failed(exc: Throwable, attachment: ByteArray) {
            println("[${this::class.simpleName}] write failed")
            writeFailed.invoke()
        }

    }
}