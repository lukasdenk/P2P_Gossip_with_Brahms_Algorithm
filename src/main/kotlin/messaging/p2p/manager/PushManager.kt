package messaging.p2p.manager

import Configs
import brahms.History
import messaging.p2p.messages.P2PMessage
import messaging.p2p.messages.PushMsg
import messaging.p2p.P2PMessageListener
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