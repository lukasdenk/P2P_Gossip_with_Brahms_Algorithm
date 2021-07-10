package p2p.manager

import Configs
import messages.p2p.P2PMessage
import messages.p2p.PushMsg
import p2p.P2PMessageListener
import p2p.brahms.History
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