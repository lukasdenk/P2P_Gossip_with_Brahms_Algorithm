package main

import messaging.p2p.Peer

object Configs {

    //    TODO: find good value
    val difficulty: Int = 4

    val cacheSize: Int = 10
    val probeInterval = 5L
    val historySize = 50
    val updateInterval = 10000L
    val pullLimit = (updateInterval / 1000L).toInt()

    val alpha = 0.45
    val beta = 0.45

    //    TODO: read from configs
    val seed: MutableSet<Peer> = mutableSetOf()
    val self = Peer("127.0.0.1", 1234)


}