package icu.takeneko.interact.network

import icu.takeneko.interact.Mod
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.*
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.spongepowered.asm.service.MixinService
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.writeText

private val logger = MixinService.getService().getLogger("NekoService")

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
        logger.debug("Received: $line")
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
                Response(request.service, request.requestId,1, "Service ${request.service} not found.", mapOf())
            }
            sendResponse(resp)
        }
    }

    override fun run() {
        try{
            val file = Mod.privateDir / "port"
            file.deleteIfExists()
            file.createFile()
            file.writeText(communicationPort.toString())
            httpServer = embeddedServer(CIO, port = communicationPort, host = "localhost", module = Application::module)
            logger.info("[NekoInteractAPI] Socket Port: $communicationPort")
            httpServer.start(wait = true)
        }catch (_:InterruptedException){}
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
            val requestString = json.encodeToString<Request>(request)
            logger.debug("Sending $requestString")
            currentConnection!!.send(requestString + "\n")
            val future = CompletableFuture<Response>()
            responseHandlers[request.requestId] = {
                if (it.status != 0) {
                    future.completeExceptionally(RemoteException(it.status, it.message))
                }else{
                    future.complete(it)
                }
            }
            future
        }
    }

    fun shutdown() {
        logger.info("[NekoInteractAPI] Stopping!")
        sleep(100) // ensure MCDReforged has received service state change
        this.interrupt()
    }
}

private fun Application.configureRouting() {
    routing {
        webSocket {
            try{
                NekoService.currentConnection?.cancel()
                NekoService.currentConnection = this
                logger.info("New Incoming Connection ${NekoService.currentConnection.hashCode()}, discarding old connection.")
                for (frame in this.incoming) {
                    frame as? Frame.Text ?: continue
                    val text: String = frame.readText()
                    if (text.startsWith("Heartbeat_PING_")) {
                        this.send(text.replace("PING", "PONG"))
                        continue
                    }
                    if (text.startsWith("Connect_PACKET_")){
                        logger.info("Session connection established.")
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
    install(WebSockets)
    configureRouting()
}