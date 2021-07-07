package messaging.gossip

class GossipNotification(
    override val port: Int,
    val messageId: Short,
    val dataType: Short,
    val data: ByteArray
) : APIMessage(port)