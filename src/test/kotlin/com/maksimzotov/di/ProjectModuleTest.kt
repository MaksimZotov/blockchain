package com.maksimzotov.di

import com.maksimzotov.services.ClientService
import com.maksimzotov.services.NeighbourNodesService
import com.maksimzotov.services.NodeService
import com.maksimzotov.services.impl.ClientServiceImplTest
import com.maksimzotov.services.impl.NeighbourNodesServiceImpl
import com.maksimzotov.services.impl.NodeServiceImpl
import io.ktor.server.application.*
import org.koin.dsl.module

fun Application.getKoinProjectModuleTest() = module {
    factory { params ->
        NodeServiceImpl(
            application = this@getKoinProjectModuleTest,
            neighbourNodesService = params.get(),
            node = params.get()
        )
    }
    factory { params ->
        NeighbourNodesServiceImpl(
            clientService = get(),
            neighbourNodes = params.get()
        )
    }
    single {
        ClientServiceImplTest()
    }
    factory<NodeService> { params ->
        get<NodeServiceImpl> {
            params
        }
    }
    factory<NeighbourNodesService> { params ->
        get<NeighbourNodesServiceImpl> {
            params
        }
    }
    single<ClientService> { get<ClientServiceImplTest>() }
}