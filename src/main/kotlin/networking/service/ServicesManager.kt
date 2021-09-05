package networking.service

import api.APICommunicator
import json.JsonMapper
import kotlinx.serialization.ExperimentalSerializationApi
import messaging.api.APIMsg
import messaging.p2p.P2PMsg
import messaging.p2p.Peer
import networking.client.ClientsManager
import p2p.P2PCommunicator
import utils.MessageParser
import utils.portFromSocketAddressAsInt
import java.net.SocketAddress
import java.nio.ByteBuffer
import kotlin.time.ExperimentalTime

@ExperimentalSerializationApi
@ExperimentalTime
object ServicesManager {
    private lateinit var apiService: APIService
    private lateinit var p2pService: P2PService

    suspend fun startApiService(gossipAddress: String, port: Int) {
        apiService = APIService(
            address = gossipAddress,
            port = port,
            read = { address: SocketAddress, data: ByteArray ->
                val apiMessage = MessageParser().toApiMessage(ByteBuffer.wrap(data))
                APICommunicator.receive(
                    apiMessage,
                    portFromSocketAddressAsInt(address)
                )
//                println(
//                    "Received message of type: ${apiMessage.javaClass.name} from " +
//                            socketAddressToString(socketAddress = address)
//                )
            },
            connectionClosed = { socketAddress ->
                APICommunicator.channelClosed(portFromSocketAddressAsInt(socketAddress = socketAddress))
            }
        )
        apiService.start()
    }

    suspend fun startP2PService(p2pAddress: String, p2pPort: Int) {
        p2pService = P2PService(
            address = p2pAddress,
            port = p2pPort,
            read = { _: SocketAddress, data: ByteArray ->
                val message = JsonMapper.mapFromJson(data) ?: return@P2PService
                P2PCommunicator.receive(
                    message
                )
            }
        )
        p2pService.start()
    }

    fun isP2PPeerOnline(peer: Peer): Boolean {
        return p2pService.isOnline(peer.ip, peer.port)
    }

    fun sendApiMessage(msg: APIMsg, port: Int) {
        try {
            apiService.write("127.0.0.1:${port}", msg.toByteArray())
        } catch (cannotConnectEx: IllegalStateException) {
            APICommunicator.channelClosed(port)
        }
    }
}