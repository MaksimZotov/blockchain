package com.maksimzotov.services.impl

import com.maksimzotov.models.Block
import com.maksimzotov.models.Node
import com.maksimzotov.services.NeighbourNodesService
import com.maksimzotov.services.NodeService
import io.ktor.server.application.*

class NodeServiceImplTest(
    private val application: Application,
    private val neighbourNodesService: NeighbourNodesService,
    private val node: Node
) : NodeService {

    override suspend fun start() {
        TODO("Not yet implemented")
    }

    override suspend fun onBlockAdded(block: Block) {
        TODO("Not yet implemented")
    }

    override suspend fun getBlocks(): List<Block> {
        TODO("Not yet implemented")
    }

}