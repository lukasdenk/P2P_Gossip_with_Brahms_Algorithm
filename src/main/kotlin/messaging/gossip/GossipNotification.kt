package messaging.gossip

class GossipNotification(
    override val port: Int,
    val messageId: Short,
    val dataType: DataType,
    val data: ByteArray
) : APIMessage(port)