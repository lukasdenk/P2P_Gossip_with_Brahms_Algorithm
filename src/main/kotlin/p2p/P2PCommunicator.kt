package p2p

import json.JsonMapper
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.p2p.P2PMsg
import messaging.p2p.P2PMsgListener
import messaging.p2p.Peer
import networking.client.ClientsManager
import p2p.brahms.manager.PullManager
import p2p.brahms.manager.PushManager
import kotlin.time.ExperimentalTime


@ExperimentalSerializationApi
@ExperimentalTime
object P2PCommunicator : P2PMsgListener {
    val listeners: List<P2PMsgListener> = listOf(PullManager, PushManager)

    fun send(msg: P2PMsg, receiver: Peer) {
        println("send ${JsonMapper.mapToJsonString(msg)} to ${receiver.port}")
        ClientsManager.write(receiver.ip, receiver.port, JsonMapper.mapToJsonByteArray(msg))
    }

    override fun receive(msg: P2PMsg) {
        listeners.forEach {
            println("received ${JsonMapper.mapToJsonString(msg)} from ${msg.sender.port}")
            it.receive(msg)
        }
    }


}