package client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import service.Constants
import utils.ParametersReader
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
 * @param firstWrite Writes first message as soon as connection established
 */
@ExperimentalTime
@Suppress("BlockingMethodInNonBlockingContext")
class Client(
    gossipAddress: String,
    gossipPort: Int,
    private val firstWrite: ((ByteArray) -> Unit) -> Unit,
    private val read: (ByteArray, (ByteArray) -> Unit) -> Unit
) {

    companion object {
        fun setupClient(
            consoleArgs: Array<String>,
            firstWrite: ((ByteArray) -> Unit) -> Unit,
            read: (data: ByteArray, write: (ByteArray) -> Unit) -> Unit
        ): Client {
            val parametersReader = ParametersReader()
            parametersReader.read(consoleArgs)
            return Client(
                gossipAddress = parametersReader.gossipServiceAddress,
                gossipPort = parametersReader.gossipServicePort,
                firstWrite = firstWrite,
                read = read
            )
        }
    }

    private val socketScope  = CoroutineScope(Dispatchers.IO)
    private val gossipAddress: SocketAddress = InetSocketAddress(gossipAddress, gossipPort)
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
            socketChannel.connect(gossipAddress, socketChannel, ConnectionHandler(
                firstWrite,
                read,
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
            delay(Duration.Companion.seconds(Constants.ReconnectionIntervalInSec))
            connect()
        }
    }

    private class ConnectionHandler(
        private val firstWrite: ((ByteArray) -> Unit) -> Unit,
        private val read: (ByteArray, (ByteArray) -> Unit) -> Unit,
        private val connectionFailed: () -> Unit = {}
    ) : CompletionHandler<Void, AsynchronousSocketChannel> {

        private val buffer = ByteBuffer.allocate(Constants.PacketSize)
        private lateinit var socketChannel: AsynchronousSocketChannel

        override fun completed(result: Void?, socketChannel: AsynchronousSocketChannel) {
            this.socketChannel = socketChannel
            println("[${this::class.simpleName}] Connected to ${socketChannel.remoteAddress}")
            sendFirstMessage()
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
                WriteHandler(
                    writeCompleted = {
                        readData()
                    }
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
                    readCompleted = { bytes: ByteArray ->
                        read.invoke(bytes, this::write)
                    },
                    readFailed = {
                    },
                    closeChannel = this::closeChannel
                )
            )
        }

        private fun closeChannel() {
            socketChannel.close()
        }

        override fun failed(exc: Throwable?, socketChannel: AsynchronousSocketChannel) {
            println("[${this::class.simpleName}] failed to connect $exc")
            connectionFailed.invoke()
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
        private val readFailed: () -> Unit = {},
        private val closeChannel: () -> Unit = {}
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

        override fun failed(exc: Throwable, buffer: ByteBuffer) {
            println("[${this::class.simpleName}] read failed")
            readFailed.invoke()
        }

    }
}