package networking.client

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.GossipAnnounce
import utils.MessageParser
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val oneWayMessageClient = OneWayMessageClient(
            address = parametersReader.gossipServiceAddress,
            port = parametersReader.gossipServicePort,
            write = { writer ->
                val message = GossipAnnounce(
                    timeToLive = 10,
                    dataType = 1,
                    data = byteArrayOf(1, 2, 3)
                )
                writer.invoke(message.toByteArray())
            }
        )
        oneWayMessageClient.start()
    }
}