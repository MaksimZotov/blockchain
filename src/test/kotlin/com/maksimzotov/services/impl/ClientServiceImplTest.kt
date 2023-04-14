package com.maksimzotov.services.impl

import com.maksimzotov.models.Block
import com.maksimzotov.services.ClientService
import com.maksimzotov.services.NodeService

class ClientServiceImplTest: ClientService {

    companion object {
        const val FIRST_NODE_ADDRESS = "FIRST_NODE_ADDRESS"
        const val SECOND_NODE_ADDRESS = "SECOND_NODE_ADDRESS"
        const val THIRD_NODE_ADDRESS = "THIRD_NODE_ADDRESS"

        var nodeServices = mapOf<String, NodeService>()
    }

    override suspend fun getBlocks(fullAddress: String) =
        nodeServices[fullAddress]!!.getBlocks()

    override suspend fun notifyAboutAddedBlock(fullAddress: String, block: Block) =
        nodeServices[fullAddress]!!.onBlockAdded(block)
}