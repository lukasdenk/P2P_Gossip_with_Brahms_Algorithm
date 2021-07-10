package messaging.api

class GossipNotification(
    val messageId: Short,
    val dataType: DataType,
    val data: ByteArray
) : APIMessage()