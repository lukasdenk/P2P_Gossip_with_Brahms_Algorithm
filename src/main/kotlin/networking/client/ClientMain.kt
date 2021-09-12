package networking.client

import json.JsonMapper
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import main.ParametersReader
import messaging.api.GossipAnnounce
import messaging.api.GossipNotification
import messaging.api.GossipNotify
import messaging.api.GossipValidation
import messaging.p2p.PullRequest
import messaging.p2p.PullResponse
import messaging.p2p.PushMsg
import messaging.p2p.SpreadMsg
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
                ttl = 10,
                dataType = 1,
                data = byteArrayOf(1, 2, 3)
            ).toByteArray()
        ).join()
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            parametersReader.gossipServicePort,
            GossipNotification(
                messageId = 1,
                dataType = 1,
                byteArrayOf(1, 2, 3)
            ).toByteArray()
        ).join()
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            parametersReader.gossipServicePort,
            GossipNotify(
                dataType = 1
            ).toByteArray()
        ).join()
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            parametersReader.gossipServicePort,
            GossipValidation(
                messageId = 1,
                isValid = true
            ).toByteArray()
        ).join()
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            7002,
            JsonMapper.mapToJsonByteArray(PullRequest(limit = 1))
        ).join()
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            7002,
            JsonMapper.mapToJsonByteArray(PullResponse(setOf()))
        ).join()
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            7002,
            JsonMapper.mapToJsonByteArray(PushMsg(nonce = 10000L))
        ).join()
        ClientsManager.write(
            parametersReader.gossipServiceAddress,
            7002,
            JsonMapper.mapToJsonByteArray(SpreadMsg(
                dataType = 1,
                ttl = 100,
                data = byteArrayOf(1, 2, 3)
            ))
        ).join()
    }
}