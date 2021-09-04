package api.manager

import api.APICommunicator
import api.APIModule
import messaging.api.*
import messaging.p2p.P2PMessage
import messaging.p2p.P2PMessageListener
import messaging.p2p.SpreadMsg
import p2p.P2PCommunicator
import p2p.brahms.View
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime

@ExperimentalTime
object GossipManager : APIMessageListener, P2PMessageListener {
    val dataTypeToSubscribers: MutableMap<DataType, MutableSet<APIModule>> = ConcurrentHashMap()

    @Synchronized
    override fun receive(msg: APIMessage, sender: APIModule) {
        if (msg is GossipNotify) {
            val subscribers = dataTypeToSubscribers.getOrDefault(msg.dataType, HashSet())
            subscribers.add(sender)
            dataTypeToSubscribers[msg.dataType] = subscribers
        } else if (msg is GossipValidation) {
            val spreadMsg = MsgCache.remove(msg.messageId)
            if (spreadMsg != null && spreadMsg.ttl != 1 && msg.isValid) {
                spreadMsg.decrementTtl()
                spread(spreadMsg)
            }
        } else if (msg is GossipAnnounce) {
            val spreadMsg = SpreadMsg(msg.dataType, msg.ttl.toInt(), msg.data)
            spread(spreadMsg)
            sendNotification(spreadMsg)
        }
    }

    fun spread(msg: SpreadMsg) {
        View.view.stream().forEach { P2PCommunicator.send(msg, it) }
    }

    @Synchronized
    override fun receive(msg: P2PMessage) {
        if (msg is SpreadMsg) {
            sendNotification(msg)
        }
    }

    private fun sendNotification(msg: SpreadMsg) {
        val msgId = MsgIdCounter.increment()
        MsgCache.put(msgId, msg)
        val notification = GossipNotification(msg.dataType, msgId, msg.data)
        dataTypeToSubscribers[notification.dataType]?.forEach {
            APICommunicator.send(notification, it)
        }
    }

    override fun channelClosed(module: APIModule) {
        dataTypeToSubscribers.forEach { t, u ->
            u.remove(module)
        }
        dataTypeToSubscribers.entries.removeIf { it.value.isEmpty() }
//        TODO: remove after testing
        if (!dataTypeToSubscribers.filterValues { it.isEmpty() }.isEmpty()) {
            throw IllegalStateException("check stmt above")
        }
    }


}