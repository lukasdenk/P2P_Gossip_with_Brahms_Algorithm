package messaging.gossip

class GossipNotification(
    val messageId: Short,
    val dataType: DataType,
    val data: ByteArray
) : APIMessage()