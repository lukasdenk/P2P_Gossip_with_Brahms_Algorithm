package p2p.brahms.manager

import main.Configs
import messaging.p2p.P2PMessage
import messaging.p2p.P2PMessageListener
import messaging.p2p.Peer
import messaging.p2p.PushMsg
import p2p.brahms.History

object PushManager : P2PMessageListener {
    val difficulty = Configs.getConfigs().difficulty
    fun push(peers: Collection<Peer>) {
        peers.parallelStream().forEach {
//TODO: create and sign message
        }
    }

    override fun receive(msg: P2PMessage, sender: Peer) {
        if (msg is PushMsg && msg.isValid()) {
            History.next(setOf(sender))
//            TODO: update view.vPush
        }
    }


}