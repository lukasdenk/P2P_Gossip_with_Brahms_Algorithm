package networking

import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import networking.service.PreferencesReader
import networking.service.ServicesManager
import kotlin.time.ExperimentalTime

@DelicateCoroutinesApi
@ExperimentalSerializationApi
@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
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