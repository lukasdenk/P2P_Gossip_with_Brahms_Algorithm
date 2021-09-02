package networking.service

import api.manager.APIMessagesManager
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.p2p.Peer
import p2p.brahms.P2PMessagesManager
import utils.*
import java.net.SocketAddress
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

// TODO combine two main modules
@DelicateCoroutinesApi
@ExperimentalSerializationApi
@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val parametersReader = ParametersReader()
        parametersReader.read(args)
        val propertiesReader = PreferencesReader.create(parametersReader.iniConfigPath)
        ServicesManager.apiService = Service(
            address = propertiesReader.gossipServiceAddress,
            port = propertiesReader.gossipServicePort,
            read = { address: SocketAddress, data: ByteBuffer ->
                APIMessagesManager.receive(
                    MessageParser().toApiMessage(data),
                    portFromSocketAddressAsInt(address)
                )
                println(
                    "Received message of type: ${MessageParser().toApiMessage(data).javaClass.name} from " +
                            "${ipFromSocketAddress(socketAddress = address)}:" +
                            portFromSocketAddressAsString(socketAddress = address)
                )
            }
        )
        ServicesManager.p2pService = Service(
            address = propertiesReader.p2pServiceAddress,
            port = propertiesReader.p2pServicePort,
            read = { address: SocketAddress, data: ByteBuffer ->
                P2PMessagesManager.receive(
                    MessageParser().toPeerToPeerMessage(data),
                    Peer(
                        ipFromSocketAddress(address),
                        portFromSocketAddressAsString(address)
                    )
                )
                println(
                    "Received message of type: ${MessageParser().toApiMessage(data).javaClass.name} from " +
                            "${ipFromSocketAddress(socketAddress = address)}:" +
                            portFromSocketAddressAsString(socketAddress = address)
                )
            }
        )
        CoroutineScope(Dispatchers.IO).launch { ServicesManager.apiService.start() }
        CoroutineScope(Dispatchers.IO).launch { ServicesManager.p2pService.start() }.join()
    }
}