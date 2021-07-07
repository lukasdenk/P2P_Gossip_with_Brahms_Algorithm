package brahms.messaging.messages

data class PullRequest(val limit: Int) :
    P2PMessage() {

}