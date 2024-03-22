package icu.takeneko.interact.network

import com.mojang.logging.LogUtils
import icu.takeneko.interact.Mod
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedWriter
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.writeText

object NekoService : Thread("NekoInteractAPI-SocketServer") {
    private var communicationPort = Random().nextInt(30000, 65535)
    private val logger = LogUtils.getLogger()
    private val services = mutableMapOf<String, Service>()
    private val responseHandlers = ConcurrentHashMap<String, (Response) -> Unit>()
    private lateinit var outboundChannel: ByteWriteChannel
    private lateinit var inboundChannel: ByteReadChannel
    private lateinit var outboundWriter: BufferedWriter
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val available: Boolean
        get() {
            return Mod.hasMCDR && this.isAlive
        }

    override fun run() {
        val file = Mod.privateDir / "port"
        file.deleteIfExists()
        file.createFile()
        file.writeText(communicationPort.toString())
        runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", communicationPort)
            logger.info("Serving NekoService at 127.0.0.1:$communicationPort")
            while (true) {
                val socket = serverSocket.accept()
                inboundChannel = socket.openReadChannel()
                outboundChannel = socket.openWriteChannel()
                outboundWriter = outboundChannel.bufferedWriter()
                launch {
                    while (true){
                        val line = inboundChannel.readUTF8Line() ?: continue
                        val response = json.decodeFromString<Response>(line)
                        val id = response.requestId
                        if (responseHandlers.containsKey(id)){
                            val fn = responseHandlers[id] ?: continue
                            fn(response)
                            responseHandlers.remove(id)
                        }else{
                            val request = json.decodeFromString<Request>(line)
                            val resp = if (request.service in services){
                                services[request.service]!!(request)
                            } else {
                                Response(request.service, request.requestId, mapOf("error" to "Service ${request.service} not found."))
                            }
                            sendResponse(resp)
                        }
                    }
                }
            }
        }
    }

    fun registerService(id:String, service: Service){
        services[id] = service
    }

    suspend fun sendResponse(response: Response){
        withContext(Dispatchers.IO) {
            outboundWriter.write(json.encodeToString<Response>(response) + "\n")
        }
    }

    fun sendRequestForResponse(request: Request): CompletableFuture<Response> {
        if (!available) {
            throw IllegalStateException("Service Unavailable!")
        }
        outboundWriter.write(json.encodeToString<Request>(request) + "\n")
        val future = CompletableFuture<Response>()
        responseHandlers[request.requestId] = {
            future.complete(it)
        }
        return future
    }
}