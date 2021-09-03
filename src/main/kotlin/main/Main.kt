package main

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import messaging.Peer

//fun main(args: Array<String>) {
//    var msg : P2PMessage = PullResponse(setOf(Peer("a",1), Peer("b",2)))
////    var msg = PullRequest(3)
//    var s = JsonMapper.mapToJson(msg)
//    var m2 = JsonMapper.mapFromJson(s)
//    println()
//}
@Serializable
sealed class Project {
}

@Serializable
class OwnedProject(val id: Long) : Project()

fun main() {
    val data = OwnedProject(5L) // data: OwnedProject here
    val msg: json.P2PMessage = json.PullResponse(mutableSetOf(Peer("a", 1), Peer("b", 2)))
//    val msg2: messaging.p2p.P2PMessage = PullResponse(mutableSetOf(Peer("a", 1), Peer("b", 2)))
    println(Json.encodeToString(msg)) // Serializing data of compile-time type OwnedProject
//    println(Json.encodeToString(msg2))
}