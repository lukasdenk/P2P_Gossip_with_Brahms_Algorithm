package networking.service

import kotlin.time.ExperimentalTime

@ExperimentalTime
object ServicesManager {
    lateinit var apiService: Service
    lateinit var p2pService: Service

    fun sendApiMessage(socketAddress: String, message: ByteArray) {
        apiService.write(socketAddress, message)
    }

    fun sendP2PMessage(socketAddress: String, message: ByteArray) {
        p2pService.write(socketAddress, message)
    }
}