package com.maksimzotov.services

import com.maksimzotov.models.Block
import com.maksimzotov.models.CheckAddedBlockResponse

interface ClientService {
    suspend fun getBlocks(fullAddress: String): List<Block>
    suspend fun checkAddedBlock(fullAddress: String, block: Block): CheckAddedBlockResponse
}