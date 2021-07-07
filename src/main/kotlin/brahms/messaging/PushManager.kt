package brahms.messaging

import Configs
import brahms.History
import brahms.messaging.messages.G2GMessage
import brahms.messaging.messages.PushRequest
import messaging.G2GMessageListener
import peers.Peer

object PushManager : G2GMessageListener {
    val difficulty = Configs.getConfigs().difficulty
    fun push(peers: Collection<Peer>) {
        peers.parallelStream().forEach {
//TODO: create and sign message
        }
    }

    override fun receive(message: G2GMessage) {
        if (message is PushRequest && message.isValid()) {
            History.next(setOf(message.sender))
        }
    }


}