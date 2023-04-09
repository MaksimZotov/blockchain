package com.maksimzotov.services

import com.maksimzotov.models.Block

interface ClientService {
    suspend fun getBlocks(fullAddress: String): List<Block>
    suspend fun notifyAboutAddedBlock(fullAddress: String, block: Block)
}