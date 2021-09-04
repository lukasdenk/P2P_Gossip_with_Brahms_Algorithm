package messaging.api

import api.APIModule

interface APIMessageListener {
    fun receive(msg: APIMessage, sender: APIModule)

    fun channelClosed(module: APIModule)
}