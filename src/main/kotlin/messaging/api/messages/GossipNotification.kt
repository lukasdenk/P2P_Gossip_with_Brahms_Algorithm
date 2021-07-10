package messaging.api.messages

import messaging.api.DataType

class GossipNotification(
    val messageId: Short,
    val dataType: DataType,
    val data: ByteArray
) : APIMessage()