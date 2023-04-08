package com.maksimzotov.services

import com.maksimzotov.models.Block
import com.maksimzotov.models.CheckAddedBlockResponse

interface NodeService {
    suspend fun start()
    suspend fun checkAddedBlock(block: Block): CheckAddedBlockResponse
    suspend fun getBlocks(): List<Block>
}