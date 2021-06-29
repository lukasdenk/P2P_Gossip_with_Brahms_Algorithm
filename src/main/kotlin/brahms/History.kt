package brahms

import peers.Peer

class History(n: Int) {
    private val samplers = MutableList(n) { Sampler() }

    //    parallel?
    fun next(peer: Peer) {
        samplers.forEach {
            it.next(peer)
        }
    }

    //    parallel?
    fun next(peers: List<Peer>) {
        peers.forEach {
            next(it)
        }
    }

    fun get(n: Int): Set<Peer> {
        return samplers.mapNotNull(Sampler::get).shuffled().subList(0, n).toSet()
    }
}