package p2p.brahms

import main.Configs
import main.randomSubSet
import messaging.p2p.Peer
import kotlin.time.ExperimentalTime

@ExperimentalTime
object History {
    private var samplers = MutableList(Configs.degree) { Sampler() }

    fun next(peers: Set<Peer>) {
        samplers.parallelStream().forEach { s ->
            peers.forEach {
                s.next(it)
            }
        }
    }

    fun next(peer: Peer) {
        next(setOf(peer))
    }

    fun get(n: Int): Set<Peer> {
        return samplers.mapNotNull(Sampler::get).randomSubSet(n)
    }
}