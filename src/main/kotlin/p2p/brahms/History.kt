package p2p.brahms

import main.Preferences
import main.randomSubSet
import messaging.p2p.Peer
import kotlin.time.ExperimentalTime

@ExperimentalTime
object History {
    private val samplers: List<Sampler>

    init {
        val initList = mutableListOf<Sampler>()
        for (i in 0..Preferences.bootstrappingPeers.size) {
            val peer = Preferences.bootstrappingPeers.getOrNull(i)
            initList.add(Sampler(peer))
        }
        samplers = initList
    }

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