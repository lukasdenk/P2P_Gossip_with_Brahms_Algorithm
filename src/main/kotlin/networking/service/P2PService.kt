package networking.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@ExperimentalTime
@Suppress("BlockingMethodInNonBlockingContext")
class P2PService(
    private val address: String,
    private val port: Int,
    private val read: (SocketAddress, ByteArray) -> Unit
) {

    private val socketConnectionsScope = CoroutineScope(Dispatchers.IO)
    private val socketAddress: SocketAddress = InetSocketAddress(address, port)
    private val waitingForConnection = AtomicBoolean(false)
    private lateinit var serverChannel: AsynchronousServerSocketChannel

    suspend fun start() {
        createServerChannel()
        accept()
        while (true) {
            delay(Duration.seconds(10))
        }
    }

    fun isOnline(hostName: String, port: Int): Boolean {
        return isSocketAlive(hostName, port)
    }

    private fun isSocketAlive(hostName: String, port: Int): Boolean {
        val socketAddress: SocketAddress = InetSocketAddress(hostName, port)
        val socket = Socket()
        val timeoutInSec = 2
        return try {
            socket.connect(socketAddress, timeoutInSec * 1000)
            socket.close()
            true
        } catch (exception: Throwable) {
            false
        }
    }

    private suspend fun createServerChannel() {
        socketConnectionsScope.launch {
            try {
                serverChannel = AsynchronousServerSocketChannel.open()
                serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                serverChannel.bind(socketAddress)
                println("[P2PService] has been started at $socketAddress")
            } catch (ex: Throwable) {
                println("[P2PService] wasn't able to start at $address:$port")
                exitProcess(0);
            }
        }.join()
    }

    private fun accept() {
        if (waitingForConnection.getAndSet(true)) {
            return
        }
        socketConnectionsScope.launch {
            serverChannel.accept(null, ConnectionHandler(
                read = read,
                successfulConnectionAttempt = {
                    waitingForConnection.set(false)
                    accept()
                },
                failedConnectionAttempt = {
                    waitingForConnection.set(false)
                    accept()
                },
                connectionClosed = {
                    connectionClosed()
                }
            )
            )
        }
    }

    private fun connectionClosed() {
        accept()
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
            readCompleted.invoke(byteArray)
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