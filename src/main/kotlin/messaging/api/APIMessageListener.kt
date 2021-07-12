package messaging.api

interface APIMessageListener {
    fun receive(msg: APIMessage, sender: Port)
}