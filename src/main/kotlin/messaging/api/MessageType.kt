package messaging.api

enum class MessageType(val value: Short) {
    GossipAnnounce(500),
    GossipNotify(501),
    GossipNotification(502),
    GossipValidation(503),
    // TODO add 504 to all types below, because we have reserved types from 500 until 519
    SpreadMessage(0),
    PullRequest(1),
    PullResponse(2),
    PushRequest(3),
    ProbeRequest(4),
    ProbeResponse(5),
}