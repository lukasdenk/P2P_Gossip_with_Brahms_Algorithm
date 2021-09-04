package main

import messaging.p2p.Peer

object Preferences {
    var gossipServiceAddress: String = "localhost"
        private set
    var gossipServicePort: Int = 7001
        private set
    var p2pServiceAddress: String = "localhost"
        private set
    var p2pServicePort: Int = 7001
        private set
    var cacheSize: Int = 50
        private set
    var degree: Int = 30
        private set
    val peers: List<Peer>
        get() = peersList.toList()
    private val peersList: MutableList<Peer> = mutableListOf()

    fun initialize(
        gossipServiceAddress: String,
        gossipServicePort: Int,
        p2pServiceAddress: String,
        p2pServicePort: Int,
        cacheSize: Int,
        degree: Int,
        peersList: MutableList<Peer>
    ) {
        this.gossipServiceAddress = gossipServiceAddress
        this.gossipServicePort = gossipServicePort
        this.p2pServiceAddress = p2pServiceAddress
        this.p2pServicePort = p2pServicePort
        this.cacheSize = cacheSize
        this.degree = degree
        this.peersList.addAll(peersList)
    }
}