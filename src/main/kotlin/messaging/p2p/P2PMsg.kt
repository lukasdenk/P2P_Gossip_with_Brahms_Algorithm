package messaging.p2p

import kotlinx.serialization.Serializable

@Serializable
sealed class P2PMsg {
    abstract val sender: Peer
}
