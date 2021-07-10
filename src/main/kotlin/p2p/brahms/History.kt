package p2p.brahms

import main.randomSubSet
import messaging.p2p.Peer

object History {
    val n = 50
    private val samplers = MutableList(n) { Sampler() }

    fun next(peers: Set<Peer>) {
        samplers.parallelStream().forEach { s ->
            peers.forEach {
                s.next(it)
            }
        }
    }

    fun get(n: Int): Set<Peer> {
        return samplers.mapNotNull(Sampler::get).randomSubSet(n)
    }
}