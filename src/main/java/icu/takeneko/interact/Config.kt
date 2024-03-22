package icu.takeneko.interact

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.loader.api.FabricLoader
import kotlin.io.path.*

object Config {
    lateinit var data:ConfigData
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun load(){
        val file = FabricLoader.getInstance().configDir / "neko-interact-api.json"
        var create = false
        if (file.notExists()){
            file.createFile()
            file.writeText(json.encodeToString(ConfigData()))
            data = ConfigData()
            create = true
        }
        if (create){
            data = file.inputStream().use { json.decodeFromStream(it) }
            file.deleteIfExists()
            file.createFile()
            file.writeText(json.encodeToString(data))
        }
    }


    @Serializable
    data class ConfigData(
        var apiInteractPort:Int = 10086
    )
}