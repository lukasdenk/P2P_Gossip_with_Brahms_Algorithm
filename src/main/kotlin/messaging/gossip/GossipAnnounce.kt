package messaging.gossip

class GossipAnnounce(
    override val port: Int,
    val timeToLive: Byte,
    val dataType: Short,
    val data: ByteArray
) : APIMessage(port)