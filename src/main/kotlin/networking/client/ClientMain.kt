package networking.client

import kotlinx.coroutines.runBlocking
import utils.MessageParser
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val client = Client(
            gossipAddress = parametersReader.gossipServiceAddress,
            gossipPort = parametersReader.gossipServicePort,
            firstWrite = { writer ->
                writer.invoke("abcde".toByteArray())
            },
            read = { buffer: ByteBuffer, writer: (ByteArray) -> Unit ->
                println("Read message type: ${MessageParser().toApiMessage(buffer).javaClass.name}")
                if (buffer.capacity() > 0) {
                    // TODO put your custom action
                    writer.invoke("qwerty".toByteArray())
                }
            }
        )
        client.start()
    }
}