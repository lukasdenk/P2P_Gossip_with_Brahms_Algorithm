package messaging.gossip

import brahms.messaging.P2PMessageListener
import brahms.messaging.messages.P2PMessage
import brahms.messaging.messages.SpreadMsg
import peers.Peer

object NotificationManager : APIMessageListener, P2PMessageListener {
    val subscribers: MutableMap<DataType, MutableSet<Port>> = HashMap()
    val msgCache: MutableMap<MsgId, SpreadMsg> = HashMap()
    override fun receive(msg: APIMessage) {
        if (msg is GossipNotify) {
            subscribers.getOrDefault(msg.dataType, listOf(msg.port))
        } else if (msg is GossipValidation) {
            SpreadManager.spread(msgCache[msg.messageId])
        }
    }

    override fun receive(msg: P2PMessage, sender: Peer) {
        if (msg is SpreadMsg) {
            notification = GossipNotification()
        }
    }

    fun channelClosed(port: Port) {
        subscribers.forEach { t, u ->
            u.remove(port)
        }
        subscribers.entries.removeIf { it.value.isEmpty() }
//        TODO: remove after testing
        if (!subscribers.filterValues { it.isEmpty() }.isEmpty()) {
            throw IllegalStateException("check stmt above")
        }
    }


}