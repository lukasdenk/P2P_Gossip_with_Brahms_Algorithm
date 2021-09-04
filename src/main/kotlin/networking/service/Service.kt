package networking.service

import kotlinx.coroutines.*
import utils.socketAddressToString
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@ExperimentalTime
@Suppress("BlockingMethodInNonBlockingContext")
class Service(
    address: String,
    port: Int,
    private val read: (SocketAddress, ByteArray) -> Unit,
    private val connectionClosed: (String) -> Unit = {},
) {

    private val socketConnectionsScope = CoroutineScope(Dispatchers.IO)
    private val socketAddress: SocketAddress = InetSocketAddress(address, port)
    private val clientChannelMap = ConcurrentHashMap<String, AsynchronousSocketChannel>()
    private val channelToAddressMap = ConcurrentHashMap<AsynchronousSocketChannel, String>()
    private val hasSpaceForNewConnections
        get() = clientChannelMap.size < Constants.MaxConnectionsAmount
    private val waitingForConnection = AtomicBoolean(false)
    private lateinit var serverChannel: AsynchronousServerSocketChannel

    suspend fun start() {
        println("[${this::class.simpleName}] Gossip-8 service has been started at $socketAddress")
        createServerChannel()
        accept()
        while (true) {
            delay(Duration.seconds(10))
        }
    }

    fun write(socketAddress: String, message: ByteArray) {
        clientChannelMap[socketAddress]?.write(ByteBuffer.wrap(message))
            ?: throw IllegalStateException("Peer $socketAddress has not been connected")
    }

    fun isOnline(socketAddress: String): Boolean {
        return clientChannelMap.contains(socketAddress)
    }

    private suspend fun createServerChannel() {
        socketConnectionsScope.launch {
            serverChannel = AsynchronousServerSocketChannel.open()
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
            serverChannel.bind(socketAddress)
        }.join()
    }

    private fun accept() {
        waitingForConnection.set(hasSpaceForNewConnections)
        if (!hasSpaceForNewConnections) {
            return
        }
        socketConnectionsScope.launch {
            serverChannel.accept(null, ConnectionHandler(
                    read = read,
                    successfulConnectionAttempt = { clientChannel ->
                        indexChannel(clientChannel)
                        accept()
                    },
                    failedConnectionAttempt = { clientChannel ->
                        val address = channelToAddressMap[clientChannel]!!
                        println("[${this::class.simpleName}] Channel ($address) failed to connect.")
                        accept()
                    },
                    connectionClosed = {
                        connectionClosed(it)
                    }
                )
            )
        }
    }

    private fun connectionClosed(channel: AsynchronousSocketChannel) {
        val address = channelToAddressMap[channel]!!
        connectionClosed.invoke(address)
        unindexChannel(channel)
        if (!waitingForConnection.get()) {
            println("[${this::class.simpleName}] Channel ($address) has been closed. ${Constants.MaxConnectionsAmount - clientChannelMap.size} connections left")
            accept()
        }
    }

    private fun indexChannel(channel: AsynchronousSocketChannel) {
        val address = socketAddressToString(channel.remoteAddress)
        clientChannelMap[address] = channel
        channelToAddressMap[channel] = address
    }

    private fun unindexChannel(channel: AsynchronousSocketChannel) {
        val address = channelToAddressMap[channel]!!
        clientChannelMap.remove(address)
        channelToAddressMap.remove(channel)
    }

    private class ConnectionHandler(
        private val read: (SocketAddress, ByteArray) -> Unit,
        private val successfulConnectionAttempt: (AsynchronousSocketChannel) -> Unit,
        private val failedConnectionAttempt: (AsynchronousSocketChannel) -> Unit,
        private val connectionClosed: (clientChannel: AsynchronousSocketChannel) -> Unit
    ) : CompletionHandler<AsynchronousSocketChannel, Any?> {

        private val buffer = ByteBuffer.allocate(Constants.PacketSize)
        private lateinit var socketChannel: AsynchronousSocketChannel

        override fun completed(
            clientChannel: AsynchronousSocketChannel,
            attachement: Any?
        ) {
            successfulConnectionAttempt.invoke(clientChannel)
            this.socketChannel = clientChannel
            println("[${this::class.simpleName}] ${clientChannel.remoteAddress} has connected")
            readData()
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
                        read.invoke(socketChannel.remoteAddress, bytes)
                        readData()
                    },
                    closeChannel = { closeChannel() }
                )
            )
        }

        private fun closeChannel() {
            socketChannel.close()
            connectionClosed.invoke(socketChannel)
        }

        override fun failed(exc: Throwable, attachment: Any?) {
            println("[${this::class.simpleName}] ${socketChannel.remoteAddress} failed to connect")
            failedConnectionAttempt.invoke(socketChannel)
        }

    }

    private class WriteHandler(
        private val writeCompleted: () -> Unit = {},
        private val writeFailed: () -> Unit = {}
    ) : CompletionHandler<Int, ByteArray> {

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
    ) : CompletionHandler<Int, ByteBuffer> {

        override fun completed(numberOfBytesRead: Int, buffer: ByteBuffer) {
            if (numberOfBytesRead < 0) {
                closeChannel.invoke()
                return
            }
            val byteArray = readToArray(buffer)
            log(byteArray)
            readCompleted.invoke(byteArray)
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