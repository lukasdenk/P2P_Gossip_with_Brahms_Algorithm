package utils

import client.ClientMode

class ParametersReader {

    var iniConfigPath: String = ""
        private set
    var gossipServiceAddress: String = "localhost"
        private set
    var gossipServicePort: Int = 7001
        private set
    var clientMode: ClientMode = ClientMode.Announce
        private set

    fun read(args: Array<String>) {
        args.forEachIndexed { i, arg ->
            when (arg) {
                "-c" -> {
                    iniConfigPath = args.getOrElse(i + 1) { "" }
                }
                "-a" -> {
                    clientMode = ClientMode.Announce
                }
                "-n" -> {
                    clientMode = ClientMode.Notify
                }
                "-d" -> {
                    gossipServiceAddress = args.getOrElse(i + 1) { "localhost" }
                }
                "-p" -> {
                    gossipServicePort = Integer.parseInt(args.getOrElse(i + 1) { "7001" })
                }
            }
        }
    }

}