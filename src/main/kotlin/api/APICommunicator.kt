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
        println("send ${msg::class.simpleName} to ${receiver.port}")
        ServicesManager.sendApiMessage(msg, receiver.port)
    }

    fun receive(msg: APIMessage, senderPort: Int) {
        println("received apimsg of type ${msg::class.simpleName} from $senderPort")
        listeners.forEach {
            it.receive(msg, APIModule(senderPort))
        }
    }

    fun channelClosed(port: Int) {
        println("Closed channel on port $port")
        listeners.forEach {
            it.channelClosed(APIModule(port))
        }
    }
}