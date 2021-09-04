package messaging.api

import api.APIModule

interface APIMsgListener {
    fun receive(msg: APIMsg, sender: APIModule)

    fun channelClosed(module: APIModule)
}