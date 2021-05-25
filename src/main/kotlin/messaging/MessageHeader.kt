package messaging

import messaging.MessageType

class MessageHeader(
    val size: Short,
    val type: MessageType
)