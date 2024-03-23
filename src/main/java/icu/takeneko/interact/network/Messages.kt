package icu.takeneko.interact.network

import kotlinx.serialization.Serializable
import java.util.*


@Serializable
data class Request(
    val service: String,
    val requestId: String = UUID.randomUUID().toString(),
    val data: Map<String, String> = mapOf()
) {
    operator fun get(s: String): String? {
        return data[s]
    }
}

@Serializable
data class Response(
    val service: String,
    val requestId: String = UUID.randomUUID().toString(),
    val status: Int = 0,
    val message:String = "",
    val data: Map<String, String> = mapOf()
)

class RequestBuilder(private val service: String) {
    private val data: MutableMap<String, String> = mutableMapOf()

    val request:Request
        get() = Request(service, data = data)

    operator fun set(k: String, v: String): RequestBuilder {
        data[k] = v
        return this
    }

    operator fun Pair<String,String>.unaryPlus(){
        data += this
    }
}

fun Request(service: String, fn: RequestBuilder.() -> Unit):Request{
    return RequestBuilder(service).apply(fn).request
}

class ResponseBuilder() {
    private var service: String = ""
    private var status: Int = 0
    private var message = ""
    private var requestId: String = UUID.randomUUID().toString()
    private val data = mutableMapOf<String, String>()

    constructor(request: Request) : this() {
        this.requestId = request.requestId
        this.service = request.service
    }

    fun asResponse(): Response {
        return Response(service, requestId, status, message, data)
    }

    fun service(srv: String) {
        this.service = srv
    }

    operator fun set(k: String, v: String): ResponseBuilder {
        data[k] = v
        return this
    }

    fun fail(): ResponseBuilder {
        status = 1
        return this
    }

    fun success(): ResponseBuilder {
        status = 0
        return this
    }

}

fun Response(request: Request, fn: ResponseBuilder.() -> Unit): Response {
    return ResponseBuilder(request).apply(fn).asResponse()
}

fun Response(fn: ResponseBuilder.() -> Unit): Response {
    return ResponseBuilder().apply(fn).asResponse()
}
