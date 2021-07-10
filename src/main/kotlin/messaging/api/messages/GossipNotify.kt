package messaging.api.messages

import messaging.api.DataType

class GossipNotify(
    val dataType: DataType
) : APIMessage()