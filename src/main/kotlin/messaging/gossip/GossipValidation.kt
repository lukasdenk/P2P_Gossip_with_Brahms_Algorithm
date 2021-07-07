package messaging.gossip

class GossipValidation(
    override val port: Int,
    val messageId: Short,
    val isWellFormed: Boolean
) : APIMessage(port)