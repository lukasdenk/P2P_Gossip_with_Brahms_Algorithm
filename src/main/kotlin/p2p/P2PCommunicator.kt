package p2p

import messaging.p2p.P2PMessage
import messaging.p2p.P2PMessageListener
import messaging.p2p.Peer
import networking.service.ServicesManager
import kotlin.time.ExperimentalTime

//TODO To be done by kyrylo

object P2PCommunicator {
    //    TODO: call listener's receive()-fun for incoming messages
    val LISTENER: List<P2PMessageListener> = listOf()

//    TODO: send in launch (best would be in networking module)
@ExperimentalTime
fun send(msg: P2PMessage, receiver: Peer) {
    ServicesManager.sendP2PMessage(msg, receiver)
}




}