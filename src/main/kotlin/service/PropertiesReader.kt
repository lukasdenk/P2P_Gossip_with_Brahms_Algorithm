package service

import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.HashMap

class PropertiesReader {

    companion object {
        private const val ServiceAddressKey = "service.address"
        private const val ServicePortKey = "service.port"
        private const val SrcFolder = "src"
        private const val MainFolder = "main"
        private const val ResourcesFolder = "resources"
        private const val ServiceProperties = "service.properties"
    }

    private val map: HashMap<String, String> = HashMap()
    var serviceAddress: String = "localhost"
        private set
    var servicePort: Int = 7001
        private set

    fun init() {
        readPropertiesIntoMap(loadProperties())
    }

    private fun loadProperties(): Properties {
        val properties = Properties()
        val propertiesFilePath = System.getProperty("user.dir") +
                "${File.separator}$SrcFolder" +
                "${File.separator}$MainFolder" +
                "${File.separator}$ResourcesFolder" +
                "${File.separator}$ServiceProperties"
        val inputStream = FileInputStream(propertiesFilePath)
        properties.load(inputStream)
        return properties
    }

    private fun readPropertiesIntoMap(properties: Properties) {
        properties.keys.map { it.toString() }.forEach { key ->
            val value = properties[key]
            when(key) {
                ServiceAddressKey -> serviceAddress = value.toString()
                ServicePortKey -> servicePort = Integer.parseInt(value.toString())
                else -> map[key] = value.toString()
            }
        }
    }

}