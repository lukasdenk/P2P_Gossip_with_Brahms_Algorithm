package brahms.messaging.messages

data class PushMsg(val pow: Int) :
    P2PMessage() {
    fun isValid(): Boolean {
//        TODO: check PoW
        return false
    }
}