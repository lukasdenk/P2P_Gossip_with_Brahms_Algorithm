package api

import api.manager.GossipManager
import messaging.api.APIMessage
import messaging.api.APIMessageListener

object APICommunicator : APIMessageListener {
    val listeners: List<APIMessageListener> = listOf(GossipManager)

    fun send(msg: APIMessage, receiver: APIModule) {

    }

    override fun receive(msg: APIMessage, sender: Int) {
        listeners.forEach {
            it.receive(msg, sender)
        }
    }
}