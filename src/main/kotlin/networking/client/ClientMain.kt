package networking.client

import kotlinx.coroutines.runBlocking
import utils.ParametersReader
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
            read = { data: ByteArray, writer: (ByteArray) -> Unit ->
                if (data.isNotEmpty()) {
                    // TODO put your custom action
                    writer.invoke("qwerty".toByteArray())
                }
            }
        )
        client.start()
    }
}