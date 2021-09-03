package p2p.brahms

import messaging.P2PMessageListener
import messaging.Peer
import messaging.p2p.P2PMessage
import p2p.brahms.manager.ProbeManager
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager

class P2PMessagesManager : P2PMessageListener {
    private val listener: List<P2PMessageListener> = listOf(PullManager, PushManager, ProbeManager)

    override fun receive(msg: P2PMessage, sender: Peer) {
        listener.forEach { it.receive(msg, sender) }
    }

}