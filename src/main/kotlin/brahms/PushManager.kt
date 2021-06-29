package brahms

import Configs
import messaging.G2GMessageListener
import messaging.gossip_to_gossip.G2GMessage
import messaging.gossip_to_gossip.PushRequest
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