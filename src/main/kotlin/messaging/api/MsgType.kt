package messaging.api

enum class MsgType(val value: Short) {
    GossipAnnounce(500),
    GossipNotify(501),
    GossipNotification(502),
    GossipValidation(503),
    NseQuery(520),
    NseEstimate(521)
}