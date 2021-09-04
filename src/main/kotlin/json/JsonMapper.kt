package json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import messaging.p2p.P2PMsg
import utils.toReadableString
import java.nio.charset.Charset

@ExperimentalSerializationApi
object JsonMapper {
    fun mapToJsonByteArray(msg: P2PMsg): ByteArray {
        return mapToJsonString(msg).toByteArray(Charset.forName("utf-8"))
    }

    fun mapToJsonString(msg: P2PMsg): String {
        val string = Json.encodeToString(msg)
        return string
    }

    fun mapFromJson(raw: ByteArray): P2PMsg? {
        return try {
            Json.decodeFromString(raw.toString(Charset.forName("utf-8")))
        } catch (exception: Throwable) {
            println(
                "[${this::class.simpleName}] message with following content cannot be decoded: " +
                        raw.toReadableString()
            )
            null
        }
    }
}