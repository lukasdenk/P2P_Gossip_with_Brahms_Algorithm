package api

import api.manager.GossipManager
import messaging.api.APIMsg
import messaging.api.APIMsgListener
import networking.service.ServicesManager
import kotlin.time.ExperimentalTime

@ExperimentalTime
object APICommunicator {
    val listeners: List<APIMsgListener> = listOf(GossipManager)

    fun send(msg: APIMsg, receiver: APIModule) {
        println("send ${msg::class.simpleName} to ${receiver.port}")
        ServicesManager.sendApiMessage(msg, receiver.port)
    }

    fun receive(msg: APIMsg, senderPort: Int) {
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