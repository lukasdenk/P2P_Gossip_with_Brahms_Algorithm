package messaging

import messaging.gossip_to_gossip.G2GMessage

//TODO To be done by kyrylo

class G2GCommunicator private constructor() {
    //    TODO: call listener's receive()-fun for incoming messages
    val listener: List<G2GMessageListener> = listOf()

    fun send(message: G2GMessage) {

    }

    companion object {
        val singleton: G2GCommunicator = G2GCommunicator()
    }


}