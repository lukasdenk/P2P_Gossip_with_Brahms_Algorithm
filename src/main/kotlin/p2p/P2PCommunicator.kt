package p2p

import json.JsonMapper
import messaging.p2p.P2PMessage
import messaging.p2p.P2PMessageListener
import messaging.p2p.Peer
import networking.service.ServicesManager
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import kotlin.time.ExperimentalTime

//TODO To be done by kyrylo

@ExperimentalTime
object P2PCommunicator : P2PMessageListener {
    //    TODO: call listener's receive()-fun for incoming messages
    val listeners: List<P2PMessageListener> = listOf(PullManager, PushManager)

    //    TODO: send in launch (best would be in networking module)
    fun send(msg: P2PMessage, receiver: Peer) {
        ServicesManager.sendP2PMessage(msg, receiver)
        println("send ${JsonMapper.mapToJsonString(msg)} to ${receiver.port}")
    }

    override fun receive(msg: P2PMessage, sender: Peer) {
        listeners.forEach {
            println("received ${JsonMapper.mapToJsonString(msg)} from ${sender.port}")
            it.receive(msg, sender)
        }
    }


}