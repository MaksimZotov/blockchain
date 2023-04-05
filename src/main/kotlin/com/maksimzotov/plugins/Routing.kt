package com.maksimzotov.plugins

import com.maksimzotov.NodesInteractor
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*

fun Application.configureRouting() {
    routing {
        get("/") {
            val nodes = NodesInteractor.nodes.shuffled()
            val works = mutableListOf<Deferred<Unit>>()
            for (node in nodes) {
                works.add(async { node.start() })
            }
            coroutineScope {
                works.awaitAll()
            }
            val stringBuilder = StringBuilder()
            for (node in nodes) {
                with(stringBuilder) {
                    append("Сгенерированные нодой блоки:\n")
                    append(node.convertGeneratedBlocksToString())
                    append("\n")
                    append("Все блоки:\n")
                    append(node.convertBlocksToString())
                    append("\n")
                }
            }
            call.respondText(stringBuilder.toString())
        }
    }
}
