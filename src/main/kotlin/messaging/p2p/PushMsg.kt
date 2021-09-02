package messaging.p2p

data class PushMsg(val nonce: Long) : P2PMessage() {
}