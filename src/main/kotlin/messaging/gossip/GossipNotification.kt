package messaging.gossip

class GossipNotification(
    val messageId: Short,
    val dataType: Short,
    val data: ByteArray
)