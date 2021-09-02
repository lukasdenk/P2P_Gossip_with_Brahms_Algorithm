package networking.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@ExperimentalTime
object ClientsManager {
    suspend fun write(ip: String, port: Int, message: ByteArray) {
        OneWayMessageClient(
            address = ip,
            port = port,
            write = { writer ->
                writer.invoke(message)
            }
        ).start()
    }
}