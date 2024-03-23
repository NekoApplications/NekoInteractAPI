package icu.takeneko.interact.network

import com.mojang.logging.LogUtils
import icu.takeneko.interact.Mod
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.writeText

private val logger = LogUtils.getLogger()

object NekoService : Thread("NekoInteractAPI-SocketServer") {
    internal var currentConnection: WebSocketServerSession? = null
    private var communicationPort = Random().nextInt(30000, 65535)
    private val services = mutableMapOf<String, Service>()
    private val responseHandlers = ConcurrentHashMap<String, (Response) -> Unit>()
    private lateinit var httpServer: ApplicationEngine
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val available: Boolean
        get() {
            return Mod.hasMCDR && this.isAlive
        }

    suspend fun handleIncoming(line: String) {
        val response = json.decodeFromString<Response>(line)
        val id = response.requestId
        if (responseHandlers.containsKey(id)) {
            val fn = responseHandlers[id] ?: return
            fn(response)
            responseHandlers.remove(id)
        } else {
            val request = json.decodeFromString<Request>(line)
            val resp = if (request.service in services) {
                services[request.service]!!(request)
            } else {
                Response(request.service, request.requestId, mapOf("error" to "Service ${request.service} not found."))
            }
            sendResponse(resp)
        }
    }

    override fun run() {
        val file = Mod.privateDir / "port"
        file.deleteIfExists()
        file.createFile()
        file.writeText(communicationPort.toString())
        httpServer = embeddedServer(CIO, port = communicationPort, host = "127.0.0.1", module = Application::module)
        httpServer.start(wait = true)
    }

    fun registerService(id: String, service: Service) {
        services[id] = service
    }

    private suspend fun sendResponse(response: Response) {
        withContext(Dispatchers.IO) {
            currentConnection?.send(json.encodeToString<Response>(response) + "\n")
        }
    }

    fun sendRequestForResponse(request: Request): CompletableFuture<Response> {
        if (!available) {
            throw IllegalStateException("Service Unavailable!")
        }
        return runBlocking {
            currentConnection?.send(json.encodeToString<Request>(request) + "\n")
            val future = CompletableFuture<Response>()
            responseHandlers[request.requestId] = {
                future.complete(it)
            }
            future
        }
    }
}

private fun Application.configureRouting() {
    routing {
        webSocket {
            try{
                NekoService.currentConnection?.cancel()
                NekoService.currentConnection = this
                logger.info("New Incoming Connection ${NekoService.currentConnection}, discarding old connection.")
                for (frame in this.incoming) {
                    frame as? Frame.Text ?: continue
                    val text: String = frame.readText()
                    if (text.startsWith("Heartbeat_PING_")) {
                        this.send(text.replace("PING", "PONG"))
                        continue
                    }
                    NekoService.handleIncoming(text)
                }
            }catch (_:CancellationException){
                logger.info("Disconnecting $this.")
            }
        }
    }
}

private fun Application.module() {
    configureRouting()
}