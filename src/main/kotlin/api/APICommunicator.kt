package api

import api.manager.GossipManager
import messaging.api.APIMessage
import messaging.api.APIMessageListener
import networking.service.ServicesManager
import kotlin.time.ExperimentalTime

@ExperimentalTime
object APICommunicator : APIMessageListener {
    val listeners: List<APIMessageListener> = listOf(GossipManager)

    fun send(msg: APIMessage, receiver: APIModule) {
        ServicesManager.sendApiMessage(msg, receiver.port)
    }

    override fun receive(msg: APIMessage, sender: Int) {
        listeners.forEach {
            it.receive(msg, sender)
        }
    }
}