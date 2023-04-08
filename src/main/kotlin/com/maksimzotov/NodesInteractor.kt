package com.maksimzotov

object NodesInteractor {
    val nodes = Config.nodesIds.map { id ->
        Node(
            id = id,
            changeNonce = when (id) {
                Config.FIRST_NODE_ID -> Config.CHANGE_NONCE_RANDOM
                Config.SECOND_NODE_ID -> Config.CHANGE_NONCE_INCREMENT
                Config.THIRD_NODE_ID -> Config.CHANGE_NONCE_DECREMENT
                else -> Config.CHANGE_NONCE_RANDOM
            }
        )
    }

    fun checkAddedBlock(id: String, block: Block): Boolean {
        val responses = getNeighboursNodes(id).map { node ->
            node.checkAddedBlockFromAnotherNode(block)
        }
        val responsesWithCompetitorBlocks = responses.filter { response ->
            if (response.checked) {
                return@filter false
            }
            val indexIsLess = (response.lastBlock?.index ?: getInitialIndex()) < block.index
            if (indexIsLess) {
                return@filter false
            }
            val blocks = response.blocks ?: return@filter false
            return@filter !checkBlocksHashes(blocks)
        }
        if (responsesWithCompetitorBlocks.isNotEmpty()) {
            return false
        }
        return true
    }

    fun getCheckedBlocksWithMaxLength(id: String, currentBlocks: List<Block>) =
        getCheckedBlocksWithMaxLength(
            checkedBlocksInNodes = getCheckedBlocksInNodes(
                blocksInNodes = getBlocksInNodes(id)
            ),
            currentBlocks = currentBlocks
        )

    private fun getCheckedBlocksWithMaxLength(
        checkedBlocksInNodes: List<List<Block>>,
        currentBlocks: List<Block>
    ) = checkedBlocksInNodes
        .filter { blocks ->
            blocks.size > currentBlocks.size
        }.maxByOrNull { blocks ->
            blocks.size
        }.takeIf { blocks ->
            !blocks.isNullOrEmpty()
        } ?: currentBlocks

    private fun getCheckedBlocksInNodes(blocksInNodes: List<List<Block>>) = blocksInNodes
        .filter(::checkBlocksHashes)

    private fun getBlocksInNodes(id: String) = getNeighboursNodes(id)
        .map { node ->
            node.getBlocks()
        }

    private fun getNeighboursNodes(id: String) = nodes
        .filter { node ->
            node.id != id
        }

    private fun checkBlocksHashes(blocks: List<Block>): Boolean {
        var previousBlock: Block? = null
        var currentBlock: Block

        val iterator = blocks.iterator()
        while (iterator.hasNext()) {
            currentBlock = iterator.next()
            val check = checkHash(
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