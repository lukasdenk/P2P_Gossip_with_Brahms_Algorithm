package api

import api.manager.GossipManager
import messaging.api.APIMessage
import messaging.api.APIMessageListener
import networking.service.ServicesManager
import kotlin.time.ExperimentalTime

@ExperimentalTime
object APICommunicator {
    val listeners: List<APIMessageListener> = listOf(GossipManager)

    fun send(msg: APIMessage, receiver: APIModule) {
        ServicesManager.sendApiMessage(msg, receiver.port)
    }

    fun receive(msg: APIMessage, senderPort: Int) {
        listeners.forEach {
            it.receive(msg, APIModule(senderPort))
        }
    }

    fun channelClosed(port: Int) {
        listeners.forEach {
            it.channelClosed(APIModule(port))
        }
    }
}