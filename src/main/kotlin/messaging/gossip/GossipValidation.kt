package messaging.gossip

class GossipValidation(
    override val port: Int,
    val messageId: Short,
    val isValid: Boolean
) : APIMessage(port)