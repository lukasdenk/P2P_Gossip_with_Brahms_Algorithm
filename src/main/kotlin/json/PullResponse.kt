package json

import kotlinx.serialization.Serializable
import messaging.Peer

@Serializable
class PullResponse(val neighbourSample: Set<Peer>) : Super() {
}