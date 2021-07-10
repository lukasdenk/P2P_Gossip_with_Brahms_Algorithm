package messages.api

class GossipAnnounce(
    val timeToLive: Byte,
    val dataType: Short,
    val data: ByteArray
) : APIMessage()