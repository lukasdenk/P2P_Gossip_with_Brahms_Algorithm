package networking.client

import json.JsonMapper
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.GossipAnnounce
import messaging.p2p.ProbeRequest
import utils.ParametersReader
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            parametersReader.gossipServicePort,
            GossipAnnounce(
                timeToLive = 10,
                dataType = 1,
                data = byteArrayOf(1, 2, 3)
            ).toByteArray()
        )
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            7002,
            JsonMapper.mapToJson(ProbeRequest())
        )
    }
}