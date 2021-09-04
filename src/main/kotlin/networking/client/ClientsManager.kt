package networking.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@ExperimentalTime
object ClientsManager {
    fun write(ip: String, port: Int, message: ByteArray): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            OneWayMessageClient(
                address = ip,
                port = port,
                write = { writer ->
                    writer.invoke(message)
                }
            ).start()
        }
    }
}