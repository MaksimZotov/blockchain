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

fun Application.getKoinProjectModuleTest(
    node: Node,
    neighbourNodes: List<NeighbourNode>
) = module {
    single {
        NodeServiceImpl(
            application = this@getKoinProjectModuleTest,
            neighbourNodesService = get(),
            node= node
        )
    }
    single {
        NeighbourNodesServiceImpl(
            clientService = get(),
            neighbourNodes = neighbourNodes
        )
    }
    single {
        ClientServiceImpl(
            httpClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json()
                }
            }
        )
    }
    single<NodeService> { get<NodeServiceImpl>() }
    single<NeighbourNodesService> { get<NeighbourNodesServiceImpl>() }
    single<ClientService> { get<ClientServiceImpl>() }
}