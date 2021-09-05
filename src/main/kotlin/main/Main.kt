package main

import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import networking.service.PreferencesReader
import networking.service.ServicesManager
import p2p.brahms.View
import test.Test
import java.io.File
import kotlin.time.ExperimentalTime


@DelicateCoroutinesApi
@ExperimentalSerializationApi
@ExperimentalTime
fun main(args: Array<String>) {
//        TODO: remove
    if (args[0] != "-c") {
        val id = args[0].toInt()
        val ini = File("src/main/resources/test_inis/$id.ini")
        println(ini.absolutePath)
        ini.createNewFile()
        ini.writeText(
            "[gossip]\n" +
                    "degree = 30\n" +
                    "cache_size = 50\n" +
                    "api_address = localhost:${7050 + id}\n" +
                    "p2p_address = localhost:${7000 + id}\n" +
                    "bootstrapper = localhost:7000"
        )
        args[0] = "-c"
        args[1] = ini.absolutePath
        Test.id = id
    }


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