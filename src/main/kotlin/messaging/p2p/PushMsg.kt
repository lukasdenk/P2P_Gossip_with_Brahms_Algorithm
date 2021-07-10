package messaging.p2p

data class PushMsg(val pow: Int) :
    P2PMessage() {
    fun isValid(): Boolean {
//        TODO: check PoW
        return false
    }
}