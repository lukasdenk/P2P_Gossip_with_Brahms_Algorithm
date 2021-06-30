package service

import org.ini4j.Ini
import java.io.File
import java.io.FileInputStream

class PreferencesReader(
    private var configPath: String = ""
) {

    companion object {
        private const val Gossip = "gossip"
        private const val Degree = "degree"
        private const val CacheSize = "cache_size"
        private const val ApiAddress = "api_address"
        private const val SrcFolder = "src"
        private const val MainFolder = "main"
        private const val ResourcesFolder = "resources"
        private const val ServiceProperties = "service.ini"

        fun create(configPath: String): PreferencesReader {
            val propertiesReader = PreferencesReader(configPath)
            propertiesReader.init()
            return propertiesReader
        }
    }

    var serviceAddress: String = "localhost"
        private set
    var servicePort: Int = 7001
        private set
    var cacheSize: Int = 50
        private set
    var degree: Int = 30
        private set
    private val preferencesFilePath
        get() = System.getProperty("user.dir") +
                "${File.separator}$SrcFolder" +
                "${File.separator}$MainFolder" +
                "${File.separator}$ResourcesFolder" +
                "${File.separator}$ServiceProperties"

    private fun init() {
        readPreferencesIntoMap(loadProperties())
    }

    private fun loadProperties(): Ini {
        if (!File(configPath).exists() || File(configPath).isDirectory) {
            configPath = preferencesFilePath
        }
        return Ini(FileInputStream(configPath))
    }

    private fun readPreferencesIntoMap(preferences: Ini) {
        if (preferences[Gossip, Degree] != null) {
            degree = Integer.parseInt(preferences[Gossip, Degree])
        }
        if (preferences[Gossip, ApiAddress] != null) {
            val s = preferences[Gossip, ApiAddress]
            if (hasIpV6Address(s)) {
                serviceAddress = s.substring(s.indexOf('[') + 1, s.indexOf(']'))
                servicePort = Integer.parseInt(s.substring(s.indexOf(']') + 2))
            } else {
                serviceAddress = s.substring(0, s.indexOf(':'))
                servicePort = Integer.parseInt(s.substring(s.indexOf(':') + 1))
            }
        }
        if (preferences[Gossip, CacheSize] != null) {
            cacheSize = Integer.parseInt(preferences[Gossip, CacheSize])
        }
    }

    private fun hasIpV6Address(s: String): Boolean =
        s.contains("[") && s.contains("]")

}