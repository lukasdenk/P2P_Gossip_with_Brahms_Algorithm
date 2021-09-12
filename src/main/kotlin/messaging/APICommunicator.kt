package messaging

import api.APIModule
import api.manager.GossipManager
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.APIMsg
import messaging.api.APIMsgListener
import networking.service.ServicesManager
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object APICommunicator {
    val listeners: List<APIMsgListener> = listOf(GossipManager)

    fun send(msg: APIMsg, receiver: APIModule) {
        println("[API] send ${msg::class.simpleName} to ${receiver.port}")
        ServicesManager.sendApiMessage(msg, receiver.port)
    }

    fun receive(msg: APIMsg, senderPort: Int) {
        println("[API] received apimsg of type ${msg::class.simpleName} from $senderPort")
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