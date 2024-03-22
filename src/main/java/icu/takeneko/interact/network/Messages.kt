package icu.takeneko.interact.network

import io.ktor.util.*
import kotlinx.serialization.Serializable

@Serializable
data class Request(val service:String, val requestId:String = generateNonce(), val arguments:Map<String,String> = mapOf())

@Serializable
data class Response(val service:String, val requestId:String = generateNonce(), val results:Map<String,String> = mapOf())
