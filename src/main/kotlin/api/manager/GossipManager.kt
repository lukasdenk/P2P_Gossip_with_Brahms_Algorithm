package api.manager

import api.APICommunicator
import api.APIModule
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.*
import messaging.p2p.P2PMsg
import messaging.p2p.P2PMsgListener
import messaging.p2p.SpreadMsg
import p2p.P2PCommunicator
import p2p.brahms.View
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object GossipManager : APIMsgListener, P2PMsgListener {
    private val dataTypeToSubscribers: MutableMap<DataType, MutableSet<APIModule>> = ConcurrentHashMap()

    @Synchronized
    override fun receive(msg: APIMsg, sender: APIModule) {
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

    @Synchronized
    fun spread(msg: SpreadMsg) {
//        println("[P2P-SP] ${View.view}")
        View.view.stream().forEach { P2PCommunicator.send(msg, it) }
    }

    @Synchronized
    override fun receive(msg: P2PMsg) {
        if (msg is SpreadMsg && msg.sender in View.view) {
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

    @Synchronized
    override fun channelClosed(module: APIModule) {
        dataTypeToSubscribers.forEach { (_, u) ->
            u.remove(module)
        }
        dataTypeToSubscribers.entries.removeIf { it.value.isEmpty() }
//        TODO: remove after testing
        if (!dataTypeToSubscribers.filterValues { it.isEmpty() }.isEmpty()) {
            throw IllegalStateException("check stmt above")
        }
    }


}