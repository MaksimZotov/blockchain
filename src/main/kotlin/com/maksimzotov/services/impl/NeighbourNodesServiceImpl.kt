package com.maksimzotov.services.impl

import com.maksimzotov.checkBlocks
import com.maksimzotov.models.Block
import com.maksimzotov.models.NeighbourNode
import com.maksimzotov.services.ClientService
import com.maksimzotov.services.NeighbourNodesService

class NeighbourNodesServiceImpl(
    private val clientService: ClientService,
    private val neighbourNodes: List<NeighbourNode>
) : NeighbourNodesService {

    override suspend fun notifyAboutAddedBlock(block: Block) =
        neighbourNodes.forEach { node ->
            clientService.notifyAboutAddedBlock(
                fullAddress = node.fullAddress,
                block = block
            )
        }

    override suspend fun getCheckedBlocksWithMaxLength(blocks: List<Block>) =
        getCheckedBlocksWithMaxLength(
            checkedBlocksInNodes = getCheckedBlocksInNodes(
                blocksInNodes = getBlocksInNodes()
            ),
            blocks = blocks
        )

    fun getCheckedBlocksWithMaxLength(
        checkedBlocksInNodes: List<List<Block>>,
        blocks: List<Block>
    ) = checkedBlocksInNodes
        .filter { blocksInNode ->
            blocksInNode.size > blocks.size
        }.maxByOrNull { blocksInNode ->
            blocksInNode.size
        }.takeIf { blocksInNode ->
            !blocksInNode.isNullOrEmpty()
        } ?: blocks

    fun getCheckedBlocksInNodes(blocksInNodes: List<List<Block>>) =
        blocksInNodes
            .filter(::checkBlocksHashes)

    suspend fun getBlocksInNodes() =
        neighbourNodes
            .map { node ->
                clientService.getBlocks(
                    fullAddress = node.fullAddress
                )
            }

    fun checkBlocksHashes(blocks: List<Block>): Boolean {
        var previousBlock: Block? = null
        var currentBlock: Block

        val iterator = blocks.iterator()
        while (iterator.hasNext()) {
            currentBlock = iterator.next()
            val check = checkBlocks(
                currentBlock = currentBlock,
                previousBlock = previousBlock
            )
            if (!check) {
                return false
            }
            previousBlock = currentBlock
        }

        return true
    }
}