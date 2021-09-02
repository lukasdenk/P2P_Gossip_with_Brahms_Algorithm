package messaging.p2p

data class PullResponse(val neighbourSample: Set<Peer>) : P2PMessage