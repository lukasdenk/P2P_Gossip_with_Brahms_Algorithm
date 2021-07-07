package messaging.gossip

class GossipValidation(
    val messageId: Short,
    val isValid: Boolean
) : APIMessage()