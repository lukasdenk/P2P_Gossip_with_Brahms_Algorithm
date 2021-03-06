package p2p.brahms

import kotlinx.serialization.ExperimentalSerializationApi
import main.Preferences
import main.randomSubSet
import messaging.p2p.Peer
import java.lang.Integer.max
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object History {
    private val samplers: List<Sampler>

    init {
        val initList = mutableListOf<Sampler>()
        for (i in 0 until max(Preferences.bootstrappingPeers.size, Preferences.degree)) {
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
        return samplers.mapNotNull(Sampler::get).toSet().randomSubSet(n)
    }
}