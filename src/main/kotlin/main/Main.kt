package main

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import p2p.brahms.Bootstrapper
import p2p.brahms.History
import p2p.brahms.View


fun main() {
//    val msg: messaging.p2p.P2PMessage = messaging.p2p.PullResponse(mutableSetOf(Peer("a", 1), Peer("b", 2)))
//    println(Json.encodeToString(msg)) // Serializing data of compile-time type OwnedProject
    runBlocking {
        Bootstrapper.bootstrap()
        launch {
            History.probe()
        }
        launch {
            View.update()
        }
    }
}