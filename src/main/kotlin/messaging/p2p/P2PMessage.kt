package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
sealed class P2PMessage {
    abstract val sender: Peer
}
