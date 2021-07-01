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

    fun start() {
        connect()
    }

    private fun connect() {
        socketScope.launch {
            val socketChannel = AsynchronousSocketChannel.open()
            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
            socketChannel.setOption(StandardSocketOptions.SO_REUSEPORT, true)
            socketChannel.connect(gossipAddress, socketChannel, ConnectionHandler())
        }
    }

    private fun reconnect() {
        println("[Client] Connection has been closed.")
        println("[Client] Attempt to reconnect after ${Constants.ReconnectionIntervalInSec} sec")
        socketScope.launch {
            delay(Duration.Companion.seconds(Constants.ReconnectionIntervalInSec))
        }
    }

    private class ConnectionHandler(
        private val connectionOpened: () -> Unit = {},
        private val connectionClosed: () -> Unit = {}
    ) : CompletionHandler<Void, AsynchronousSocketChannel> {

        private val buffer = ByteBuffer.allocate(Constants.PacketSize)
        private lateinit var socketChannel: AsynchronousSocketChannel

        override fun completed(result: Void, socketChannel: AsynchronousSocketChannel) {
            this.socketChannel = socketChannel
            connectionOpened.invoke()
            sendGossipAnnounce()
        }

        private fun sendGossipAnnounce() {
            if (!socketChannel.isOpen) {
                closeChannel()
                return
            }
            socketChannel.write(
                ByteBuffer.wrap("abcde".toByteArray()),
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
            connectionClosed.invoke()
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