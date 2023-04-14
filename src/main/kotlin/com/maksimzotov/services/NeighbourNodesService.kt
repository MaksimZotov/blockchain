package com.maksimzotov.services

import com.maksimzotov.models.Block

interface NeighbourNodesService {
    suspend fun getCheckedBlocksWithMaxLength(blocks: List<Block>): List<Block>
    suspend fun notifyAboutAddedBlock(block: Block)
}