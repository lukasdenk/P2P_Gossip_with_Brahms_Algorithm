package brahms.messaging

import Configs
import brahms.History
import brahms.messaging.messages.P2PMessage
import brahms.messaging.messages.PushMsg
import peers.Peer

object PushManager : P2PMessageListener {
    val difficulty = Configs.getConfigs().difficulty
    fun push(peers: Collection<Peer>) {
        peers.parallelStream().forEach {
//TODO: create and sign message
        }
    }

    override fun receive(msg: P2PMessage, sender:Peer) {
        if (msg is PushMsg && msg.isValid()) {
            History.next(setOf(sender))
        }
    }


}