package com.maksimzotov.services.impl

import com.maksimzotov.models.Block
import com.maksimzotov.services.ClientService
import io.ktor.client.*

class ClientServiceImplTest(
    private val httpClient: HttpClient
) : ClientService {

    override suspend fun getBlocks(fullAddress: String): List<Block> {
        TODO("Not yet implemented")
    }

    override suspend fun notifyAboutAddedBlock(fullAddress: String, block: Block) {
        TODO("Not yet implemented")
    }
}