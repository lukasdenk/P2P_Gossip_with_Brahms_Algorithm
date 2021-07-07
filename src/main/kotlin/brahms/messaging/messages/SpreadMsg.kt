package brahms.messaging.messages

import messaging.gossip.DataType
import peers.Peer

data class SpreadMsg(override val sender: Peer, override val receiver: Peer, val dataType: DataType, var ttl: Byte) :
    P2PMessage(sender, receiver) {
//    fun copy(sender: Peer, receiver: Peer) = SpreadMsg(sender, receiver)
}