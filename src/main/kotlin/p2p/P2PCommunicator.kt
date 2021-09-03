package p2p

import messaging.p2p.P2PMessage
import messaging.p2p.P2PMessageListener
import messaging.p2p.Peer
import networking.service.ServicesManager
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import kotlin.time.ExperimentalTime

//TODO To be done by kyrylo

object P2PCommunicator : P2PMessageListener {
    //    TODO: call listener's receive()-fun for incoming messages
    val listeners: List<P2PMessageListener> = listOf(PullManager, PushManager)

    //    TODO: send in launch (best would be in networking module)
    @ExperimentalTime
    fun send(msg: P2PMessage, receiver: Peer) {
        ServicesManager.sendP2PMessage(msg, receiver)
    }

    override fun receive(msg: P2PMessage, sender: Peer) {
        listeners.forEach {
            it.receive(msg, sender)
        }
    }


}