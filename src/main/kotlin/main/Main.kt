package main

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import messaging.p2p.Peer

//fun main(args: Array<String>) {
//
//}


fun main() {
    val msg: messaging.p2p.P2PMessage = messaging.p2p.PullResponse(mutableSetOf(Peer("a", 1), Peer("b", 2)))
    println(Json.encodeToString(msg)) // Serializing data of compile-time type OwnedProject
//    println(Json.encodeToString(msg2))
}