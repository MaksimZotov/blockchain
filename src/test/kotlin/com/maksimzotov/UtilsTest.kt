package com.maksimzotov

import com.maksimzotov.di.getKoinProjectModuleTest
import com.maksimzotov.models.NeighbourNode
import com.maksimzotov.models.Node
import com.maksimzotov.services.NeighbourNodesService
import com.maksimzotov.services.NodeService
import com.maksimzotov.services.impl.ClientServiceImplTest
import com.maksimzotov.services.impl.NodeServiceImpl
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.get

const val NODE_1_IP = "NODE_1_IP"
const val NODE_2_IP = "NODE_2_IP"
const val NODE_3_IP = "NODE_3_IP"

private inline fun testWithInjection(
    crossinline block: Application.() -> Unit
) = testApplication {
    application {
        startKoin {
            modules(
                getKoinProjectModuleTest()
            )
        }
        block(this@application)
        stopKoin()
    }
}

fun test(
    fullAddressOfMainBlock: String = ClientServiceImplTest.FIRST_NODE_ADDRESS,
    block: (
        neighbourNodesService1: NeighbourNodesService,
        neighbourNodesService2: NeighbourNodesService,
        neighbourNodesService3: NeighbourNodesService,
        nodeService1: NodeService,
        nodeService2: NodeService,
        nodeService3: NodeService,
        nodeServiceImpl: NodeServiceImpl
    ) -> Unit
) = testWithInjection {
    val node1 = Node(
        ip = NODE_1_IP,
        port = 1,
        changeNonce = "r",
        generateFirstBlock = fullAddressOfMainBlock == ClientServiceImplTest.FIRST_NODE_ADDRESS
    )
    val node2 = Node(
        ip = NODE_2_IP,
        port = 2,
        changeNonce = "d",
        generateFirstBlock = fullAddressOfMainBlock == ClientServiceImplTest.SECOND_NODE_ADDRESS
    )
    val node3 = Node(
        ip = NODE_3_IP,
        port = 3,
        changeNonce = "i",
        generateFirstBlock = fullAddressOfMainBlock == ClientServiceImplTest.THIRD_NODE_ADDRESS
    )

    val neighbourNode1 = NeighbourNode(
        fullAddress = ClientServiceImplTest.FIRST_NODE_ADDRESS
    )
    val neighbourNode2 = NeighbourNode(
        fullAddress = ClientServiceImplTest.SECOND_NODE_ADDRESS
    )
    val neighbourNode3 = NeighbourNode(
        fullAddress = ClientServiceImplTest.THIRD_NODE_ADDRESS
    )

    val neighbourNodesService1 = get<NeighbourNodesService> {
        parametersOf(listOf(neighbourNode2, neighbourNode3))
    }
    val neighbourNodesService2 = get<NeighbourNodesService> {
        parametersOf(listOf(neighbourNode1, neighbourNode3))
    }
    val neighbourNodesService3 = get<NeighbourNodesService> {
        parametersOf(listOf(neighbourNode1, neighbourNode2))
    }

    val nodeService1 = get<NodeService> {
        parametersOf(neighbourNodesService1, node1)
    }
    val nodeService2 = get<NodeService> {
        parametersOf(neighbourNodesService2, node2)
    }
    val nodeService3 = get<NodeService> {
        parametersOf(neighbourNodesService3, node3)
    }

    ClientServiceImplTest.nodeServices = mapOf(
        ClientServiceImplTest.FIRST_NODE_ADDRESS to nodeService1,
        ClientServiceImplTest.SECOND_NODE_ADDRESS to nodeService2,
        ClientServiceImplTest.THIRD_NODE_ADDRESS to nodeService3
    )

    val nodeServiceImpl = get<NodeServiceImpl> {
        parametersOf(neighbourNodesService1, node1)
    }

    block(
        neighbourNodesService1, neighbourNodesService2, neighbourNodesService3,
        nodeService1, nodeService2, nodeService3,
        nodeServiceImpl
    )
}