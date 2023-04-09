package com.maksimzotov.services

import com.maksimzotov.models.Block

interface NodeService {
    suspend fun start()
    suspend fun onBlockAdded(block: Block)
    suspend fun getBlocks(): List<Block>
}