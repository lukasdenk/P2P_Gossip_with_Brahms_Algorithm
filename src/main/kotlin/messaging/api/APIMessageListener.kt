package messaging.api

import messaging.api.messages.APIMessage

interface APIMessageListener {
    fun receive(msg: APIMessage, sender: Port)
}