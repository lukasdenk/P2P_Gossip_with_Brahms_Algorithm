package brahms.messaging.messages

import messaging.gossip.DataType

data class SpreadMsg(val dataType: DataType, var ttl: Byte) :
    P2PMessage() {
//    fun copy(sender: Peer, receiver: Peer) = SpreadMsg(sender, receiver)
}