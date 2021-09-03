package networking.service

import kotlinx.coroutines.*
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
    private val read: (SocketAddress, ByteArray) -> Unit
) {

    private val socketConnectionsScope = CoroutineScope(Dispatchers.IO)
    private val socketAddress: SocketAddress = InetSocketAddress(address, port)
    private val clientChannelMap = ConcurrentHashMap<String, AsynchronousSocketChannel>()
    private val channelToAddressMap = ConcurrentHashMap<AsynchronousSocketChannel, String>()
    private val clientChannelList = ConcurrentLinkedQueue<AsynchronousSocketChannel>()
    private val hasSpaceForNewConnections
        get() = clientChannelList.size < Constants.MaxConnectionsAmount
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
            ?: throw IllegalStateException("Peer has not been connected")
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
            serverChannel.accept(
                clientChannelList, ConnectionHandler(
                    read = read,
                    successfulConnectionAttempt = { clientChannel ->
                        clientChannelList.add(clientChannel)
                        clientChannelMap[clientChannel.remoteAddress.toString()] = clientChannel
                        channelToAddressMap[clientChannel] = clientChannel.remoteAddress.toString()
                        accept()
                    },
                    failedConnectionAttempt = { clientChannel ->
                        clientChannelList.remove(clientChannel)
                        clientChannelMap.remove(clientChannel.remoteAddress.toString())
                        channelToAddressMap.remove(clientChannel)
                        accept()
                    },
                    connectionClosed = {
                        connectionClosed(it)
                    }
                )
            )
        }
    }

    private fun connectionClosed(channel: AsynchronousSocketChannel?) {
        // TODO investigate why sometimes we do not have channel in channelToAddressMap
        if (channelToAddressMap.contains(channel)) {
            clientChannelMap.remove(channelToAddressMap[channel])
        }
        channelToAddressMap.remove(channel)
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
            this.socketChannel = clientChannel
            println("[${this::class.simpleName}] ${clientChannel.remoteAddress} has connected")
            readData()
            successfulConnectionAttempt.invoke(clientChannel)
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