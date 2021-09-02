package main

import messaging.p2p.Peer

object Configs {

    //    TODO: find good value
    val difficulty: Int = 4
    val cacheSize: Int = -1
    val probeTimeout = 4
    val probeInterval = 5L
    val kickInterval = 5L
    val seed: List<Peer> = listOf()


    val self = Peer("127.0.0.1", 1234)


}