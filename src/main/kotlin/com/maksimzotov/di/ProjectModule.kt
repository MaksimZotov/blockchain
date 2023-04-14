package com.maksimzotov.di

import com.maksimzotov.models.NeighbourNode
import com.maksimzotov.models.Node
import com.maksimzotov.services.ClientService
import com.maksimzotov.services.NeighbourNodesService
import com.maksimzotov.services.NodeService
import com.maksimzotov.services.impl.ClientServiceImpl
import com.maksimzotov.services.impl.NeighbourNodesServiceImpl
import com.maksimzotov.services.impl.NodeServiceImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import org.koin.dsl.module

fun Application.getKoinProjectModule(
    node: Node,
    neighbourNodes: List<NeighbourNode>
) = module {
    single<NodeService> {
        NodeServiceImpl(
            application = this@getKoinProjectModule,
            neighbourNodesService = get(),
            node= node
        )
    }
    single<NeighbourNodesService> {
        NeighbourNodesServiceImpl(
            clientService = get(),
            neighbourNodes = neighbourNodes
        )
    }
    single<ClientService> {
        ClientServiceImpl(
            httpClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json()
                }
            }
        )
    }
}