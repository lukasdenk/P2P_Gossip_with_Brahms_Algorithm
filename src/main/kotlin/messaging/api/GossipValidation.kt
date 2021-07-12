package messaging.api

class GossipValidation(
    val messageId: Short,
    val isValid: Boolean
) : APIMessage()