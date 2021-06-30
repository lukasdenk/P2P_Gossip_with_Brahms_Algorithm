package service

import kotlinx.coroutines.*
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@Suppress("BlockingMethodInNonBlockingContext")
class Service(
    address: String,
    port: Int
) {

    private val socketConnectionsScope = CoroutineScope(Dispatchers.IO)
    private val socketAddress: SocketAddress = InetSocketAddress(address, port)
    private val clientChannelList = LinkedList<AsynchronousSocketChannel>()

    fun start() {
        accept()
    }

    @OptIn(ExperimentalTime::class)
    private fun accept() {
        if (clientChannelList.size >= Constants.MaxConnectionsAmount) {
            return
        }
        socketConnectionsScope.launch {
            val serverChannel = AsynchronousServerSocketChannel.open()
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
            serverChannel.setOption(StandardSocketOptions.SO_REUSEPORT, true)
            serverChannel.bind(socketAddress)
            serverChannel.accept(clientChannelList, ConnectionHandler { accept() })
        }
    }

    fun cancel() {
        socketConnectionsScope.cancel()
    }

    private class ConnectionHandler(
        private val callback: () -> Unit
    ): CompletionHandler<AsynchronousSocketChannel, LinkedList<AsynchronousSocketChannel>> {

        override fun completed(clientChannel: AsynchronousSocketChannel,
                               channelsList: LinkedList<AsynchronousSocketChannel>) {
            println("Connection handler: connection accepted")
            channelsList.add(clientChannel)
            if (clientChannel.isOpen) {
                val buffer = ByteBuffer.allocate(Constants.PacketSize)
                clientChannel.read(buffer, buffer, CommunicationHandler())
            }
            callback.invoke()
            // TODO close connection after socket is closed and remove it from list
        }

        override fun failed(exc: Throwable, attachment: LinkedList<AsynchronousSocketChannel>) {
            callback.invoke()
        }

    }

    private class CommunicationHandler: CompletionHandler<Int, ByteBuffer> {
        override fun completed(numberOfBytesRead: Int, buffer: ByteBuffer) {
            if (numberOfBytesRead < 0) {
                return
            }
            println("Some data received ($numberOfBytesRead)")
            // TODO As we have a message, start to wait for another one
        }

        override fun failed(exc: Throwable?, attachment: ByteBuffer) {
            TODO("Not yet implemented")
        }
    }

}