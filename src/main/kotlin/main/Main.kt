package main

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
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val propertiesReader = PreferencesReader.create(parametersReader.iniConfigPath)
        Preferences.initialize(
            gossipServiceAddress = propertiesReader.gossipServiceAddress,
            gossipServicePort = propertiesReader.gossipServicePort,
            p2pServiceAddress = propertiesReader.p2pServiceAddress,
            p2pServicePort = propertiesReader.p2pServicePort,
            cacheSize = propertiesReader.cacheSize,
            degree = propertiesReader.degree,
            peersList = propertiesReader.peersList
        )
        launch {
            View.update()
        }
        mutableListOf(
            CoroutineScope(Dispatchers.IO).launch {
                ServicesManager.startApiService(
                    Preferences.gossipServiceAddress,
                    Preferences.gossipServicePort
                )
            },
            CoroutineScope(Dispatchers.IO).launch {
                ServicesManager.startP2PService(
                    Preferences.p2pServiceAddress,
                    Preferences.p2pServicePort
                )
            }
        ).joinAll()
    }
}