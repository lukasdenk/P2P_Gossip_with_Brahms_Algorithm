package test

import java.io.File

fun main(array: Array<String>) {
    val id = array[0].toInt()
    val ini = File("resources/$id.ini")
    ini.writeText(
        "[gossip]\n" +
                "degree = 30\n" +
                "cache_size = 50\n" +
                "api_address = localhost:${7001 + id * 2}\n" +
                "p2p_address = localhost:${7002 + 2 * id}\n" +
                "bootstrapper = localhost:7000"
    )


}