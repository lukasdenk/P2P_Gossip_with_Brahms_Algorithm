package messaging

import messaging.gossip_to_gossip.G2GMessage

interface G2GMessageListener {
    fun receive(message: G2GMessage)
}