package networking

enum class P2PMessageType(val value: Int) {
    // TODO add 504 to all types below, because we have reserved types from 500 until 519
    SpreadMessage(0),
    PullRequest(1),
    PullResponse(2),
    PushRequest(3),
    ProbeRequest(4),
    ProbeResponse(5),
}