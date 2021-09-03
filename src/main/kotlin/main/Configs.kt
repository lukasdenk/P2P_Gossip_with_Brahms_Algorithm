package main

import messaging.p2p.Peer

object Configs {

    //    TODO: find good value
    val difficulty: Int = 4
    //    TODO: what is cacheSize for?
    val cacheSize: Int = -1
    val probeTimeout = 4
    val probeInterval = 5L
    val kickInterval = 5L
    val historySize = 50
    val pullLimit = 20
    val updateInterval = 20000L

    //    TODO: read from configs
    val seed: MutableSet<Peer> = mutableSetOf()
    val self = Peer("127.0.0.1", 1234)

}