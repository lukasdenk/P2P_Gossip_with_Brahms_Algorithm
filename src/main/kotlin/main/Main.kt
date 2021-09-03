package main

import kotlinx.coroutines.*
import networking.service.PreferencesReader
import networking.service.ServicesManager
import utils.ParametersReader
import kotlin.time.ExperimentalTime


@ExperimentalTime
fun main(args: Array<String>) {
//    val msg: messaging.p2p.P2PMessage = messaging.p2p.PullResponse(mutableSetOf(Peer("a", 1), Peer("b", 2)))
//    println(Json.encodeToString(msg)) // Serializing data of compile-time type OwnedProject
    runBlocking {
//        Bootstrapper.bootstrap()
//        launch {
//            History.probe()
//        }
//        launch {
//            View.update()
//        }
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val propertiesReader = PreferencesReader.create(parametersReader.iniConfigPath)
        mutableListOf(
            CoroutineScope(Dispatchers.IO).launch {
                ServicesManager.startApiService(
                    propertiesReader.gossipServiceAddress,
                    propertiesReader.gossipServicePort
                )
            },
            CoroutineScope(Dispatchers.IO).launch {
                ServicesManager.startP2PService(
                    propertiesReader.p2pServiceAddress,
                    propertiesReader.p2pServicePort
                )
            }
        ).joinAll()
    }
}