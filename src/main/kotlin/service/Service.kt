package service

import kotlinx.coroutines.*
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.ExperimentalTime


@ExperimentalTime
@Suppress("BlockingMethodInNonBlockingContext")
class Service(
    address: String,
    port: Int,
    private val read: (ByteArray, (ByteArray) -> Unit) -> Unit,
    private val firstWrite: ((ByteArray) -> Unit) -> Unit = {}
) {

    private val socketConnectionsScope = CoroutineScope(Dispatchers.IO)
    private val socketAddress: SocketAddress = InetSocketAddress(address, port)
    // TODO if we have ID for a client, change structure to a map
    private val clientChannelList = ConcurrentLinkedQueue<AsynchronousSocketChannel>()
    private val hasSpaceForNewConnections
        get() = clientChannelList.size < Constants.MaxConnectionsAmount
    private val waitingForConnection = AtomicBoolean(false)

    fun start() {
        println("[${this::class.simpleName}] Gossip-8 service has been started at $socketAddress")
        accept()
    }

    private fun accept() {
        waitingForConnection.set(hasSpaceForNewConnections)
        if (!hasSpaceForNewConnections) {
            return
        }
        socketConnectionsScope.launch {
            val serverChannel = AsynchronousServerSocketChannel.open()
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
            serverChannel.setOption(StandardSocketOptions.SO_REUSEPORT, true)
            serverChannel.bind(socketAddress)
            serverChannel.accept(clientChannelList, ConnectionHandler(
                firstWrite = firstWrite,
                read = read,
                connectionAttemptFinished = { accept() },
                connectionClosed = this@Service::connectionClosed
            ))
        }
    }

    private fun connectionClosed(channel: AsynchronousSocketChannel) {
        clientChannelList.remove(channel)
        if (!waitingForConnection.get()) {
            println("[${this::class.simpleName}] Channel is closed. ${Constants.MaxConnectionsAmount - clientChannelList.size} connections left")
            accept()
        }
    }

    fun cancel() {
        socketConnectionsScope.cancel()
    }

    private class ConnectionHandler(
        private val firstWrite: ((ByteArray) -> Unit) -> Unit,
        private val read: (ByteArray, (ByteArray) -> Unit) -> Unit,
        private val connectionAttemptFinished: () -> Unit,
        private val connectionClosed: (clientChannel: AsynchronousSocketChannel) -> Unit
    ): CompletionHandler<AsynchronousSocketChannel, ConcurrentLinkedQueue<AsynchronousSocketChannel>> {

        private val buffer = ByteBuffer.allocate(Constants.PacketSize)
        private lateinit var socketChannel: AsynchronousSocketChannel

        override fun completed(clientChannel: AsynchronousSocketChannel,
                               channelsList: ConcurrentLinkedQueue<AsynchronousSocketChannel>) {
            this.socketChannel = clientChannel
            channelsList.add(clientChannel)
            println("[${this::class.simpleName}] ${clientChannel.remoteAddress} has connected")
            readData()
            connectionAttemptFinished.invoke()
        }

        private fun sendFirstMessage() {
            if (!socketChannel.isOpen) {
                closeChannel()
                return
            }
            firstWrite.invoke { bytes: ByteArray ->
                write(bytes)
            }
        }

        private fun write(bytes: ByteArray) {
            socketChannel.write(
                ByteBuffer.wrap(bytes),
                bytes,
                WriteHandler(writeCompleted = { readData() })
            )
        }

        private fun readData() {
            if (!socketChannel.isOpen) {
                closeChannel()
                return
            }
            buffer.clear()
            socketChannel.read(buffer, Constants.MessageTimeoutInSec, TimeUnit.SECONDS, buffer,
                ReadHandler(
                    readCompleted = { bytes: ByteArray ->
                        read.invoke(bytes, this::write)
                    },
                    closeChannel = { closeChannel() }
                )
            )
        }

        private fun closeChannel() {
            socketChannel.close()
            connectionClosed.invoke(socketChannel)
        }

        override fun failed(exc: Throwable, attachment: ConcurrentLinkedQueue<AsynchronousSocketChannel>) {
            println("[${this::class.simpleName}] ${socketChannel.remoteAddress} failed to connect")
            connectionAttemptFinished.invoke()
        }

    }

    private class WriteHandler(
        private val writeCompleted: () -> Unit = {},
        private val writeFailed: () -> Unit = {}
    ): CompletionHandler<Int, ByteArray> {

        override fun completed(result: Int, attachment: ByteArray) {
            log(attachment)
            writeCompleted.invoke()
        }

        private fun log(bytes: ByteArray) {
            println(
                "[${this::class.simpleName}] sent " +
                        bytes.map(Byte::toInt).joinToString(separator = " ") { String.format("%02X", it) }
            )
        }

        override fun failed(exc: Throwable, attachment: ByteArray) {
            println("[${this::class.simpleName}] write failed")
            writeFailed.invoke()
        }

    }

    private class ReadHandler(
        private val readCompleted: (ByteArray) -> Unit = {},
        private val closeChannel: () -> Unit
    ): CompletionHandler<Int, ByteBuffer> {

        override fun completed(numberOfBytesRead: Int, buffer: ByteBuffer) {
            if (numberOfBytesRead < 0) {
                closeChannel.invoke()
                return
            }
            val data = readToArray(buffer)
            log(data)
            readCompleted.invoke(data)
        }

        private fun log(arr: ByteArray) {
            println(
                "[${this::class.simpleName}] incoming msg (${arr.size}): " +
                        arr.map(Byte::toInt).joinToString(" ") { String.format("%02X", it) }
            )
        }

        private fun readToArray(buffer: ByteBuffer): ByteArray {
            val arr = ByteArray(buffer.position())
            buffer.position(0)
            buffer.get(arr, 0, arr.size)
            return arr
        }

        override fun failed(exc: Throwable?, attachment: ByteBuffer) {
            println("[CommunicationHandler] Failed to read the data: $exc")
            closeChannel.invoke()
        }

    }

}