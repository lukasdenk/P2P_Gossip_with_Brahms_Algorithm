package messaging.p2p.messages

data class PullRequest(val limit: Int) :
    P2PMessage() {

}