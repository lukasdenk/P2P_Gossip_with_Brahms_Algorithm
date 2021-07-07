package messaging.gossip

class GossipNotify(
    override val port: Int,
    val dataType: DataType
) : APIMessage(port)