package messaging.gossip

class GossipNotify(
    override val port: Int,
    val dataType: Short
) : APIMessage(port)