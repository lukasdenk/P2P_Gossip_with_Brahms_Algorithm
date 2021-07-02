package client

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import service.PreferencesReader
import service.Service
import utils.ParametersReader
import java.nio.ByteBuffer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val client = Client.setupClient(
            consoleArgs = args,
            firstWrite = { writer ->
                writer.invoke("abcde".toByteArray())
            },
            read = { data: ByteArray, writer: (ByteArray) -> Unit ->
                if (data.isNotEmpty()) {
                    // TODO put your custom action
                    writer.invoke("qwerty".toByteArray())
                }
            }
        )
        client.start()
    }
}