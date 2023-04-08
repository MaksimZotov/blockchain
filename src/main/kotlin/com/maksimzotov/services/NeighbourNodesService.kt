package com.maksimzotov.services

import com.maksimzotov.models.Block
import com.maksimzotov.models.NeighbourNode

interface NeighbourNodesService {
    suspend fun getCheckedBlocksWithMaxLength(blocks: List<Block>): List<Block>
    suspend fun checkAddedBlock(block: Block): Boolean
}