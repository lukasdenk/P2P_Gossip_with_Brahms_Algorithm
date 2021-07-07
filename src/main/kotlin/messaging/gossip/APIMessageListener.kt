package messaging.gossip

interface APIMessageListener {
    fun receive(msg: APIMessage, sender: Port)
}