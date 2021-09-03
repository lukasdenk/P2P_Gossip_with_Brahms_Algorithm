package main

import api.APIModule
import messaging.p2p.Peer

object Configs {

    //    TODO: find good value
    val difficulty: Int = 4

    val probeInterval = 5000L
    val updateInterval = 10000L
    val pullLimit = (updateInterval / 1000L).toInt()
    const val pushLimit: Int = 1000
    val estimationUpdateInterval = 50
    const val initNse3rdRoot = 100
    const val estimationInterval = 10L * 60L * 1000L

    //    TODO: read from configs
    val seed: MutableSet<Peer> = mutableSetOf()
    val self = Peer("127.0.0.1", 1234)
    val cacheSize: Int = 10
    val gossipModule = APIModule(7001)
    val nseModule = APIModule(7201)

}