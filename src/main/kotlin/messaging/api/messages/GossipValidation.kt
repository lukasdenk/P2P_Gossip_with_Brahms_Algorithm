package messaging.api.messages

class GossipValidation(
    val messageId: Short,
    val isValid: Boolean
) : APIMessage()