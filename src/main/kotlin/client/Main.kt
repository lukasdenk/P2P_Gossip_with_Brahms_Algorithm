package client

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        println("Gossip-8 client has been started")
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val client = Client(
            gossipAddress = parametersReader.gossipServiceAddress,
            gossipPort = parametersReader.gossipServicePort,
            write = { writer ->
                writer.invoke("abcde".toByteArray())
            },
            read = { data: ByteArray, writer: (ByteArray) -> Unit ->
                if (data.isNotEmpty()) {
                    writer.invoke("qwerty".toByteArray())
                }
            }
        )
        client.start()
        while (client.up) {
            delay(Duration.seconds(10))
        }
    }
}