package com.maksimzotov.routing

import com.maksimzotov.Configs
import com.maksimzotov.services.NodeService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureNodeRouting() {

    val nodeService by inject<NodeService>()

    routing {
        get(Configs.REQUEST_GET_BLOCKS) {
            call.respond(nodeService.getBlocks())
        }
        post(Configs.REQUEST_NOTIFY_ABOUT_ADDED_BLOCK) {
            call.respond(nodeService.onBlockAdded(call.receive()))
        }
    }
}