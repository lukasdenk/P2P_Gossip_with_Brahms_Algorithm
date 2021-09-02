package networking.service

import api.manager.APIMessagesManager
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.p2p.Peer
import p2p.brahms.P2PMessagesManager
import utils.MessageParser
import utils.ipFromSocketAddress
import utils.portFromSocketAddressAsInt
import utils.portFromSocketAddressAsString
import java.net.SocketAddress
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object ServicesManager {
    private lateinit var apiService: Service
    private lateinit var p2pService: Service

    suspend fun startApiService(address: String, port: Int) {
        apiService = Service(
            address = address,
            port = port,
            read = { address: SocketAddress, data: ByteBuffer ->
                val apiMessage = MessageParser().toApiMessage(data)
                APIMessagesManager.receive(
                    apiMessage,
                    portFromSocketAddressAsInt(address)
                )
                println(
                    "Received message of type: ${apiMessage.javaClass.name} from " +
                            "${ipFromSocketAddress(socketAddress = address)}:" +
                            portFromSocketAddressAsString(socketAddress = address)
                )
            }
        )
        apiService.start()
    }

    suspend fun startP2PService(p2pAddress: String, p2pPort: Int) {
        p2pService = Service(
            address = p2pAddress,
            port = p2pPort,
            read = { address: SocketAddress, data: ByteBuffer ->
                val message = MessageParser().toPeerToPeerMessage(data)
                P2PMessagesManager.receive(
                    message,
                    Peer(
                        ipFromSocketAddress(address),
                        portFromSocketAddressAsString(address)
                    )
                )
                println(
                    "Received message of type: ${message.javaClass.name} from " +
                            "${ipFromSocketAddress(socketAddress = address)}:" +
                            portFromSocketAddressAsString(socketAddress = address)
                )
            }
        )
        p2pService.start()
    }

    fun sendApiMessage(socketAddress: String, message: ByteArray) {
        apiService.write(socketAddress, message)
    }

    fun sendP2PMessage(socketAddress: String, message: ByteArray) {
        p2pService.write(socketAddress, message)
    }
}