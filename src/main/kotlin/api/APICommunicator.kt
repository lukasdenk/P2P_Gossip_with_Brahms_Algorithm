package api

import api.manager.GossipNotifyManager
import messaging.api.APIMessage
import messaging.api.APIMessageListener

object APICommunicator : APIMessageListener {
    val listeners: List<APIMessageListener> = listOf(GossipNotifyManager)

    fun send(msg: APIMessage, receiver: APIModule) {

    }

    override fun receive(msg: APIMessage, sender: Int) {
        listeners.forEach {
            it.receive(msg, sender)
        }
    }
}