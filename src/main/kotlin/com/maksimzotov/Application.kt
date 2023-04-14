package com.maksimzotov

import com.maksimzotov.di.getKoinProjectModule
import com.maksimzotov.models.NeighbourNode
import com.maksimzotov.models.Node
import com.maksimzotov.routing.configureNodeRouting
import com.maksimzotov.services.NodeService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.cli.*
import kotlinx.coroutines.launch
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
        install(Koin) {
            modules(
                getKoinProjectModule(
                    node = node,
                    neighbourNodes = neighbourNodes
                )
            )
        }
        install(ContentNegotiation) {
            json()
        }
        configureNodeRouting()

        val nodeService by inject<NodeService>()
        launch {
            nodeService.start()
        }

    }.start(wait = true)
}