package api

import messages.api.APIMessage
import messages.api.Port

interface APIMessageListener {
    fun receive(msg: APIMessage, sender: Port)
}