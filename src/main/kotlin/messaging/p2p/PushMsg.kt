package messaging.p2p

class PushMsg(val nonce: ByteArray) :
    P2PMessage() {
    fun isValid(): Boolean {
//        TODO: check PoW
        return false
    }
}