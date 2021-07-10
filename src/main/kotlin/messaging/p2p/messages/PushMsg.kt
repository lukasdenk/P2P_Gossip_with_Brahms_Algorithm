package messaging.p2p.messages

data class PushMsg(val pow: Int) :
    P2PMessage() {
    fun isValid(): Boolean {
//        TODO: check PoW
        return false
    }
}