package brahms.messaging.messages

import peers.Peer

data class PushMsg(override val sender: Peer, override val receiver: Peer, val work: Int) :
    P2PMessage(sender, receiver) {
    fun isValid(): Boolean {
//        TODO: check PoW
        return false
    }
}