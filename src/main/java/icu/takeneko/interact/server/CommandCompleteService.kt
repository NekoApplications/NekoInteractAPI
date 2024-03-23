package icu.takeneko.interact.server

import com.mojang.brigadier.CommandDispatcher
import icu.takeneko.interact.network.Request
import icu.takeneko.interact.network.Response
import icu.takeneko.interact.network.Service
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource

class CommandCompleteService(private val server:MinecraftServer): Service {

    private val dispatcher: CommandDispatcher<ServerCommandSource> = server.commandManager.dispatcher
    override suspend fun handleRequest(request: Request): Response {
        val command = request["command"] ?: ""
        val cursor = request["cursor"]?.toIntOrNull() ?: command.length
        val future = dispatcher.getCompletionSuggestions(dispatcher.parse(command, server.commandSource), cursor)
        val sug = future.get()
        val m = if (!sug.isEmpty)sug.list.map { it.text } else emptyList()
        return Response(request) {
            this["suggestions"] = Json.encodeToString<List<String>>(m)
        }
    }
}