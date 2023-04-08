package com.maksimzotov

import com.maksimzotov.models.NeighbourNode
import com.maksimzotov.models.Node
import com.maksimzotov.routing.configureNodeRouting
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
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.cli.*
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    val parser = ArgParser("Blockchain")

    val nodeParams by parser.option(
        ArgType.String,
        shortName = "a",
        description = "Укажите адрес вашей ноды в виде -a 0.0.0.0:8080"
    ).required()

    val generateFirstBlockParams by parser.option(
        ArgType.Boolean,
        shortName = "g",
        description = "Укажите флаг -g, если ваша нода должна создать первый блок"
    ).default(false)

    val nonceChangerParams by parser.option(
        ArgType.String,
        shortName = "c",
        description = """Укажите, как нужно изменять nonce при подборе хэша
            r - Случайное число
            i - Инкремент
            d - Декремент
        """.trimMargin()
    ).required()

    val neighbourNodesParams by parser.option(
        ArgType.String,
        shortName = "n",
        description = "Укажите адреса соседних нод в виде -n http://0.0.0.0:8081 -n http://0.0.0.0:8082 ..."
    ).multiple()

    parser.parse(args)

    val nodeIpAndPort = nodeParams.split(':')
    val node = Node(
        ip = nodeIpAndPort[0],
        port = nodeIpAndPort[1].toInt(),
        changeNonce = nonceChangerParams,
        generateFirstBlock = generateFirstBlockParams
    )

    val neighbourNodes = neighbourNodesParams.map { fullAddress ->
        NeighbourNode(fullAddress)
    }

    embeddedServer(
        factory = Netty,
        port = node.port,
        host = node.ip,
    ) {

        val projectModule = module {
            single<NodeService> {
                NodeServiceImpl(
                    application = this@embeddedServer,
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

        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json()
        }

        install(Koin) {
            modules(projectModule)
        }

        configureNodeRouting()

        val nodeService by inject<NodeService>()
        launch {
            nodeService.start()
        }

    }.start(wait = true)
}