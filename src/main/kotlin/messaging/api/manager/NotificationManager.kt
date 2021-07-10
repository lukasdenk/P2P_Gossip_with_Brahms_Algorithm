package messaging.api.manager

import messaging.api.*
import messaging.api.messages.APIMessage
import messaging.api.messages.GossipNotification
import messaging.api.messages.GossipNotify
import messaging.api.messages.GossipValidation
import messaging.p2p.manager.SpreadManager
import messaging.p2p.P2PMessageListener
import messaging.p2p.messages.P2PMessage
import messaging.p2p.messages.SpreadMsg
import peers.Peer

object NotificationManager : APIMessageListener, P2PMessageListener {
    val subscribers: MutableMap<DataType, MutableSet<Port>> = HashMap()
    val msgCache: MutableMap<MsgId, SpreadMsg> = HashMap()


    override fun receive(msg: APIMessage, sender: Port) {
        if (msg is GossipNotify) {
            subscribers.getOrDefault(msg.dataType, HashSet()).add(sender)
        } else if (msg is GossipValidation) {
            val spreadMsg = msgCache[msg.messageId]
            if (spreadMsg != null && spreadMsg.ttl != 1) {
                spreadMsg.decrementTtl()
                SpreadManager.spread(spreadMsg)
            }
        }
    }

    //    not thread safe
    override fun receive(msg: P2PMessage, sender: Peer) {
        if (msg is SpreadMsg) {
            val msgId = MsgIdCounter.increment()
            msgCache.put(msgId, msg)
            val notification = GossipNotification(msg.dataType, msgId, msg.data)
            subscribers[notification.dataType]?.forEach { APICommunicator.send(notification, it) }
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