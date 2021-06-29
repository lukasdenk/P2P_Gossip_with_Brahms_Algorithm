package brahms

import peers.Peer

class Sampler {
    var peer: Peer? = null
    var h: String? = null

    fun initialize() {
//        rand h
        this.peer = null
    }

    fun next(peer: Peer) {
//        TODO
    }

    fun get(): Peer? {
        return peer
    }

}