package client

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import service.PreferencesReader
import service.Service
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val client = setupClient(
            consoleArgs = args,
            firstWrite = { writer ->
                writer.invoke("abcde".toByteArray())
            },
            read = { data: ByteArray, writer: (ByteArray) -> Unit ->
                if (data.isNotEmpty()) {
                    // TODO put your custom action
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

@ExperimentalTime
private fun setupClient(
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