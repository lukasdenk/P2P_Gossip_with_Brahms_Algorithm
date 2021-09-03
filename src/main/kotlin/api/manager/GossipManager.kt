package api.manager

import messaging.api.APIMessage
import messaging.api.APIMessageListener
import messaging.api.DataType
import messaging.api.gossip.GossipAnnounce
import messaging.api.gossip.GossipNotification
import messaging.api.gossip.GossipNotify
import messaging.api.gossip.GossipValidation
import messaging.p2p.P2PMessage
import messaging.p2p.P2PMessageListener
import messaging.p2p.Peer
import messaging.p2p.SpreadMsg
import networking.service.ServicesManager
import p2p.P2PCommunicator
import p2p.brahms.View
import java.util.*
import kotlin.time.ExperimentalTime

object GossipManager : APIMessageListener, P2PMessageListener {
    val subscribers: MutableMap<DataType, MutableSet<Int>> = Collections.synchronizedMap(mutableMapOf())


    @ExperimentalTime
    override fun receive(msg: APIMessage, sender: Int) {
        if (msg is GossipNotify) {
            subscribers.getOrDefault(msg.dataType, HashSet()).add(sender)
        } else if (msg is GossipValidation) {
            val spreadMsg = MsgCache.remove(msg.messageId)
            if (spreadMsg != null && spreadMsg.ttl != 1) {
                spreadMsg.decrementTtl()
                spread(spreadMsg)
            }
        } else if (msg is GossipAnnounce) {
            val spreadMsg = SpreadMsg(msg.dataType, msg.ttl.toInt(), msg.data)
            spread(spreadMsg)
        }
    }


    @ExperimentalTime
    fun spread(msg: SpreadMsg) {
        View.view.stream().forEach { P2PCommunicator.send(msg, it) }
    }

    @ExperimentalTime
    @Synchronized
    override fun receive(msg: P2PMessage, sender: Peer) {
        if (msg is SpreadMsg) {
            val msgId = MsgIdCounter.increment()
            MsgCache.put(msgId, msg)
            val notification = GossipNotification(msg.dataType, msgId, msg.data)
            subscribers[notification.dataType]?.forEach {
                ServicesManager.sendApiMessage(notification, it)
            }
        }
    }

    fun channelClosed(port: Int) {
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