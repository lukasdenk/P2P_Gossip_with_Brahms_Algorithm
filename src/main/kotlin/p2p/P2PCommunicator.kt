package p2p

import api.manager.GossipManager
import json.JsonMapper
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.p2p.P2PMsg
import messaging.p2p.P2PMsgListener
import messaging.p2p.Peer
import messaging.p2p.SpreadMsg
import networking.client.ClientsManager
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import kotlin.time.ExperimentalTime


@ExperimentalSerializationApi
@ExperimentalTime
object P2PCommunicator : P2PMsgListener {
    val listeners: List<P2PMsgListener> = listOf(PullManager, PushManager, GossipManager)

    fun send(msg: P2PMsg, receiver: Peer) {
        if (msg is SpreadMsg) {
            println("[P2P] send ${JsonMapper.mapToJsonString(msg)} to ${receiver.port}")
        }
        ClientsManager.write(receiver.ip, receiver.port, JsonMapper.mapToJsonByteArray(msg))
    }

    override fun receive(msg: P2PMsg) {
        if (msg is SpreadMsg) {
            println("[P2P] received ${JsonMapper.mapToJsonString(msg)} from ${msg.sender.port}")
        }
        listeners.forEach {
            it.receive(msg)
        }
    }


}