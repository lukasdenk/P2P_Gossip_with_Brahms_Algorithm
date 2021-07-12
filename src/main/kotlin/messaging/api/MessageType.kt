package messaging.api

enum class MessageType(val value: Short) {
    GossipAnnounce(500),
    GossipNotify(501),
    GossipNotification(502),
    GossipValidation(503)
}