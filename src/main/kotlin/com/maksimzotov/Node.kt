package com.maksimzotov

import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.random.nextUInt

class Node(
    val id: String,
    private val changeNonce: String
) {
    private var blocks = mutableListOf<Block>()
    private val generatedBlocks = mutableListOf<Block>()

    private val changeNonceLambda: (Int) -> Int = { nonce ->
        when (changeNonce) {
            Config.CHANGE_NONCE_RANDOM -> Random.nextInt()
            Config.CHANGE_NONCE_INCREMENT -> nonce + 1
            Config.CHANGE_NONCE_DECREMENT -> nonce - 1
            else -> Random.nextInt()
        }
    }

    suspend fun start() {
        repeat(Config.STUB_NODE_ATTEMPTS_COUNT) {
            delay(Random.nextLong(Config.STUB_DELAY))
            blocks = NodesInteractor.getCheckedBlocksWithMaxLength(id, blocks).toMutableList()
            val generatedBlock = generateNextBlock(
                data = getRandomString(Config.DATA_LENGTH),
                previousBlock = blocks.lastOrNull(),
                changeNonce = changeNonceLambda
            )
            if (NodesInteractor.checkAddedBlock(id, generatedBlock)) {
                blocks.add(generatedBlock)
                generatedBlocks.add(generatedBlock)
            }
        }
    }

    fun checkAddedBlockFromAnotherNode(block: Block) = CheckAddedBlockResponse(
        blocks =
            if (checkHash(block, blocks.lastOrNull()))
                null
            else
                blocks
    )

    fun convertBlocksToString() =
        convertBlocksToString(blocks)

    fun convertGeneratedBlocksToString() =
        convertBlocksToString(generatedBlocks)

    fun getBlocks() =
        blocks.toList()

    private fun convertBlocksToString(blocks: List<Block>): String {
        val stringBuilder = StringBuilder("$id:\n")
        for (block in blocks) {
            stringBuilder.append("$block\n")
        }
        return stringBuilder.toString()
    }
}
