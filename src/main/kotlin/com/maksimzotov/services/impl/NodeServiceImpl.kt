package com.maksimzotov.services.impl

import com.maksimzotov.*
import com.maksimzotov.models.Block
import com.maksimzotov.models.CheckAddedBlockResponse
import com.maksimzotov.models.Node
import com.maksimzotov.services.NeighbourNodesService
import com.maksimzotov.services.NodeService
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class NodeServiceImpl(
    private val application: Application,
    private val neighbourNodesService: NeighbourNodesService,
    private val node: Node
) : NodeService {

    private var blocks = mutableListOf<Block>()
    private val generatedBlocks = mutableListOf<Block>()

    private val changeNonceLambda: (Int) -> Int = { nonce ->
        when (node.changeNonce) {
            "r" -> Random.nextInt()
            "i" -> nonce + 1
            "d" -> nonce - 1
            else -> Random.nextInt()
        }
    }

    private val mutex = Mutex()

    override suspend fun start() {
        while (true) {
            blocks = try {
                neighbourNodesService.getCheckedBlocksWithMaxLength(blocks).toMutableList()
            } catch (_: Exception) {
                delay(Configs.FAILED_REQUEST_DELAY)
                continue
            }

            val generatedBlock = generateNextBlock(
                data = getRandomString(Configs.DATA_LENGTH),
                previousBlock = blocks.lastOrNull(),
                changeNonce = changeNonceLambda
            )

            mutex.withLock {
                blocks.add(generatedBlock)
                generatedBlocks.add(generatedBlock)

                val checked = try {
                    neighbourNodesService.checkAddedBlock(generatedBlock)
                } catch (_: Exception) {
                    blocks.removeLast()
                    generatedBlocks.removeLast()
                    return@withLock
                }

                if (!checked) {
                    blocks.removeLast()
                    generatedBlocks.removeLast()
                } else {
                    printBlocks()
                }
            }
        }
    }

    override suspend fun checkAddedBlock(block: Block) = mutex.withLock {
        CheckAddedBlockResponse(
            blocks =
                if (checkHash(currentBlock = block, previousBlock = blocks.lastOrNull()))
                    null
                else
                    blocks
        )
    }

    override suspend fun getBlocks() = mutex.withLock {
        blocks
    }

    private fun printBlocks() {
        val stringBuilder = StringBuilder()
        with(stringBuilder) {
            append("\n\nСгенерированные нодой блоки:\n")
            append(convertGeneratedBlocksToString())
            append("\n")
            append("Все блоки:\n")
            append(convertBlocksToString())
            append("\n\n")
        }
        application.log.info(stringBuilder.toString())
    }

    private fun convertBlocksToString() =
        convertBlocksToString(blocks)

    private fun convertGeneratedBlocksToString() =
        convertBlocksToString(generatedBlocks)

    private fun convertBlocksToString(blocks: List<Block>): String {
        val stringBuilder = StringBuilder(
            with(node) { "$ip:$port $generateFirstBlock\n" }
        )
        for (block in blocks) {
            stringBuilder.append("$block\n")
        }
        return stringBuilder.toString()
    }
}