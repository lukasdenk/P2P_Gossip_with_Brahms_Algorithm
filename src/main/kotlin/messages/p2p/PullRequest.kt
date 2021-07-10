package messages.p2p

data class PullRequest(val limit: Int) :
    P2PMessage() {

}