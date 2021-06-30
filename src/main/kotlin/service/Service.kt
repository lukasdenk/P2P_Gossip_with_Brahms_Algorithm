package service

import kotlinx.coroutines.*
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.channels.InterruptedByTimeoutException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.ExperimentalTime


@Suppress("BlockingMethodInNonBlockingContext")
class Service(
    address: String,
    port: Int
) {

    private val socketConnectionsScope = CoroutineScope(Dispatchers.IO)
    private val socketAddress: SocketAddress = InetSocketAddress(address, port)
    private val clientChannelList = ConcurrentLinkedQueue<AsynchronousSocketChannel>()
    private val hasSpaceForNewConnections
        get() = clientChannelList.size < Constants.MaxConnectionsAmount

    fun start() {
        accept()
    }

    @OptIn(ExperimentalTime::class)
    private fun accept() {
        if (!hasSpaceForNewConnections) {
            return
        }
        socketConnectionsScope.launch {
            val serverChannel = AsynchronousServerSocketChannel.open()
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
            serverChannel.setOption(StandardSocketOptions.SO_REUSEPORT, true)
            serverChannel.bind(socketAddress)
            serverChannel.accept(clientChannelList, ConnectionHandler(
                connectionAttemptFinished = { accept() },
                connectionClosed = this@Service::connectionClosed
            ))
        }
    }

    private fun connectionClosed(channel: AsynchronousSocketChannel) {
        println("[Service] Channel is closed")
        if (!hasSpaceForNewConnections) {
            clientChannelList.remove(channel)
            accept()
        } else {
            clientChannelList.remove(channel)
        }
    }

    fun cancel() {
        socketConnectionsScope.cancel()
    }

    private class ConnectionHandler(
        private val connectionAttemptFinished: () -> Unit,
        private val connectionClosed: (clientChannel: AsynchronousSocketChannel) -> Unit
    ): CompletionHandler<AsynchronousSocketChannel, ConcurrentLinkedQueue<AsynchronousSocketChannel>> {

        private val buffer = ByteBuffer.allocate(Constants.PacketSize)
        private lateinit var clientChannel: AsynchronousSocketChannel
        private val communicationHandler = CommunicationHandler(
            readingCompleted = { readData() },
            closeChannel = { closeChannel() }
        )

        override fun completed(clientChannel: AsynchronousSocketChannel,
                               channelsList: ConcurrentLinkedQueue<AsynchronousSocketChannel>) {
            this.clientChannel = clientChannel
            channelsList.add(clientChannel)
            println("[ConnectionHandler] ${clientChannel.remoteAddress} has connected")
            readData()
            connectionAttemptFinished.invoke()
        }

        private fun readData() {
            if (clientChannel.isOpen) {
                clientChannel.read(buffer, Constants.MessageTimeoutInSec, TimeUnit.SECONDS, buffer, communicationHandler)
            } else {
                closeChannel()
            }
        }

        private fun closeChannel() {
            clientChannel.close()
            connectionClosed.invoke(clientChannel)
        }

        override fun failed(exc: Throwable, attachment: ConcurrentLinkedQueue<AsynchronousSocketChannel>) {
            println("[ConnectionHandler] ${clientChannel.remoteAddress} failed to connect")
            connectionAttemptFinished.invoke()
        }

    }

    private class CommunicationHandler(
        private val readingCompleted: () -> Unit,
        private val closeChannel: () -> Unit
    ): CompletionHandler<Int, ByteBuffer> {

        private val isWorking: AtomicBoolean = AtomicBoolean(true)

        override fun completed(numberOfBytesRead: Int, buffer: ByteBuffer) {
            if (!isWorking.get()) {
                return
            }
            if (numberOfBytesRead < 0) {
                stop()
                return
            }
            val sb = StringBuilder()
            val arr = ByteArray(buffer.position())
            buffer.position(0)
            buffer.get(arr, 0, arr.size)
            arr.map(Byte::toInt).map{ String.format("%02X", it) }.forEach { sb.append(it).append(" ") }
            println("[CommunicationHandler] incoming msg ($numberOfBytesRead): $sb")
            readingCompleted.invoke()
        }

        override fun failed(exc: Throwable?, attachment: ByteBuffer) {
            println("[CommunicationHandler] Failed to read the data: $exc")
            stop()
        }

        fun stop() {
            isWorking.set(false)
            closeChannel.invoke()
        }
    }

}