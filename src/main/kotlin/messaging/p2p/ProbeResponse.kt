package messaging.p2p

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("b")
class ProbeResponse() : P2PMessage() {
}