package main

import api.manager.NseMsgsManager
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import networking.service.PreferencesReader
import networking.service.ServicesManager
import p2p.brahms.View
import utils.ParametersReader
import kotlin.time.ExperimentalTime


@DelicateCoroutinesApi
@ExperimentalSerializationApi
@ExperimentalTime
fun main(args: Array<String>) {
//    val msg: messaging.p2p.P2PMessage = messaging.p2p.PullResponse(mutableSetOf(Peer("a", 1), Peer("b", 2)))
//    println(Json.encodeToString(msg)) // Serializing data of compile-time type OwnedProject
    runBlocking {
        launch {
            View.update()
        }
        launch {
            NseMsgsManager.estimate()
        }
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