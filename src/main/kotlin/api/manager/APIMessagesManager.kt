package api.manager

import messaging.api.*
import messaging.api.gossip.GossipNotification
import messaging.api.gossip.GossipNotify
import messaging.api.gossip.GossipValidation
import messaging.p2p.P2PMessage
import messaging.p2p.P2PMessageListener
import messaging.p2p.Peer
import messaging.p2p.SpreadMsg
import networking.service.ServicesManager
import p2p.SpreadManager
import kotlin.time.ExperimentalTime

object APIMessagesManager : APIMessageListener, P2PMessageListener {
    val subscribers: MutableMap<DataType, MutableSet<Port>> = HashMap()
    val msgCache: MutableMap<MsgId, SpreadMsg> = HashMap()


    @ExperimentalTime
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
    @ExperimentalTime
    override fun receive(msg: P2PMessage, sender: Peer) {
        if (msg is SpreadMsg) {
            val msgId = MsgIdCounter.increment()
            msgCache.put(msgId, msg)
            val notification = GossipNotification(msg.dataType, msgId, msg.data)
            subscribers[notification.dataType]?.forEach {
                ServicesManager.sendApiMessage(notification, it)
            }
        }
    }

    //    TODO: call when connection breaks
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