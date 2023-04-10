package com.maksimzotov.services.impl

import com.maksimzotov.models.Block
import com.maksimzotov.models.NeighbourNode
import com.maksimzotov.services.ClientService
import com.maksimzotov.services.NeighbourNodesService

class NeighbourNodesServiceImplTest(
    private val clientService: ClientService,
    private val neighbourNodes: List<NeighbourNode>
) : NeighbourNodesService {

    override suspend fun notifyAboutAddedBlock(block: Block) {
        TODO("Not yet implemented")
    }

    override suspend fun getCheckedBlocksWithMaxLength(blocks: List<Block>): List<Block> {
        TODO("Not yet implemented")
    }
}