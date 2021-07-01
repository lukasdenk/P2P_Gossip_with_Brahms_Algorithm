package client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.Constants
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
@Suppress("BlockingMethodInNonBlockingContext")
class Client(
    gossipAddress: String,
    gossipPort: Int
) {

    private val socketScope  = CoroutineScope(Dispatchers.IO)
    private val gossipAddress: SocketAddress = InetSocketAddress(gossipAddress, gossipPort)
    private val reconnectedTimes = AtomicInteger(0)

    fun start() {
        connect()
    }

    private fun connect() {
        socketScope.launch {
            val socketChannel = AsynchronousSocketChannel.open()
            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
            socketChannel.setOption(StandardSocketOptions.SO_REUSEPORT, true)
            socketChannel.connect(gossipAddress, socketChannel, ConnectionHandler(
                connectionFailed = { reconnect() }
            ))
        }
    }

    private fun reconnect() {
        println("[Client] Failed to connect.")
        if (reconnectedTimes.getAndAdd(1) > Constants.MaxReconnectAttempts) {
            // TODO add callback to finish the program
            println("[Client] Was not able to connect to the gossip service.")
            return
        }
        println("[Client] Attempt to reconnect after ${Constants.ReconnectionIntervalInSec} sec")
        socketScope.launch {
            delay(Duration.Companion.seconds(Constants.ReconnectionIntervalInSec))
            connect()
        }
    }

    private class ConnectionHandler(
        private val connectionOpened: () -> Unit = {},
        private val connectionClosed: () -> Unit = {},
        private val connectionFailed: () -> Unit = {}
    ) : CompletionHandler<Void, AsynchronousSocketChannel> {

        private val buffer = ByteBuffer.allocate(Constants.PacketSize)
        private lateinit var socketChannel: AsynchronousSocketChannel

        override fun completed(result: Void, socketChannel: AsynchronousSocketChannel) {
            // TODO investigate why passed socket channel is null
            this.socketChannel = socketChannel
            println("[ConnectionHandler] Connected to ${socketChannel.remoteAddress}")
            connectionOpened.invoke()
            sendGossipAnnounce()
        }

        private fun sendGossipAnnounce() {
            if (!socketChannel.isOpen) {
                closeChannel()
                return
            }
            val dummyArray = "abcde".toByteArray()
            val sb = StringBuilder()
            dummyArray.map(Byte::toInt).map{ String.format("%02X", it) }.forEach { sb.append(it).append(" ") }
            println("[ConnectionHandler] Sending gossip announce $sb")
            socketChannel.write(
                ByteBuffer.wrap(dummyArray),
                null,
                WriteHandler(
                    writeCompleted = { readData() },
                    writeFailed = { sendGossipAnnounce() }
                )
            )
        }

        private fun readData() {
            if (!socketChannel.isOpen) {
                closeChannel()
                return
            }
            buffer.clear()
            socketChannel.read(
                buffer,
                buffer,
                ReadHandler(
                    readCompleted = { readData() },
                    readFailed = { readData() }
                )
            )
        }

        private fun closeChannel() {
            socketChannel.close()
            connectionClosed.invoke()
        }

        override fun failed(exc: Throwable, socketChannel: AsynchronousSocketChannel) {
            connectionFailed.invoke()
        }

    }

    private class WriteHandler(
        private val writeCompleted: () -> Unit = {},
        private val writeFailed: () -> Unit = {}
    ): CompletionHandler<Int, Any?> {

        override fun completed(result: Int, attachment: Any?) {
            writeCompleted.invoke()
        }

        override fun failed(exc: Throwable, attachment: Any?) {
            writeFailed.invoke()
        }

    }

    private class ReadHandler(
        private val readCompleted: () -> Unit = {},
        private val readFailed: () -> Unit = {}
    ): CompletionHandler<Int, ByteBuffer> {

        override fun completed(result: Int, buffer: ByteBuffer) {
            readCompleted.invoke()
        }

        override fun failed(exc: Throwable, buffer: ByteBuffer) {
            readFailed.invoke()
        }

    }
}