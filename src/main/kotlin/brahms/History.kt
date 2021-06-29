package brahms

import peers.Peer

object History {
    val n = 50
    private val samplers = MutableList(n) { Sampler() }

    //    parallel?
    fun next(peers: Set<Peer>) {
        peers.forEach { p ->
            samplers.forEach { s ->
                s.next(p)
            }
        }
    }

    fun get(n: Int): Set<Peer> {
        return samplers.mapNotNull(Sampler::get).shuffled().subList(0, n).toSet()
    }
}