package service

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import utils.ParametersReader
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    runBlocking {
        val service = setupService(
            consoleArgs = args,
            read = { data: ByteArray, writer: (ByteArray) -> Unit ->
                if (data.isNotEmpty()) {
                    // TODO to write something in response use writer
                    //  writer.invoke("qwerty".toByteArray())
                }
            }
        )
        service.start()
        while (true) {
            delay(Duration.seconds(10))
        }
    }
}

@ExperimentalTime
private fun setupService(
    consoleArgs: Array<String>,
    read: (data: ByteArray, write: (ByteArray) -> Unit) -> Unit
): Service {
    val parametersReader = ParametersReader()
    parametersReader.read(consoleArgs)
    val propertiesReader = PreferencesReader.create(parametersReader.iniConfigPath)
    return Service(
        propertiesReader.serviceAddress,
        propertiesReader.servicePort,
        read
    )
}