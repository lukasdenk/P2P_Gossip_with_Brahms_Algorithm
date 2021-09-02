package networking.service

import kotlin.time.ExperimentalTime

@ExperimentalTime
object ServicesManager {
    lateinit var apiService: Service
    lateinit var p2pService: Service
}