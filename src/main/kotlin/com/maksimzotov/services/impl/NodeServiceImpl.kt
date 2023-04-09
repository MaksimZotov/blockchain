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
            try {
                mutex.withLock {
                    blocks = neighbourNodesService.getCheckedBlocksWithMaxLength(blocks).toMutableList()
                }
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
                if (generatedBlock.index > (blocks.lastOrNull()?.index ?: Configs.INITIAL_INDEX)) {
                    blocks.add(generatedBlock)
                }
            }

            try {
                neighbourNodesService.checkAddedBlock(generatedBlock)
            } catch (_: Exception) { }

            printCurrentState(generatedBlock.index)
        }
    }

    override suspend fun checkAddedBlock(block: Block) = mutex.withLock {
        val checked = checkHash(currentBlock = block, previousBlock = blocks.lastOrNull())
        if (checked) {
            blocks.add(block)
        }
        CheckAddedBlockResponse(
            blocks = if (checked) null else blocks
        )
    }

    override suspend fun getBlocks() = mutex.withLock {
        blocks
    }

    private fun printCurrentState(index: Int) {
        val stringBuilder = StringBuilder(
            with(node) {
                "\n\nНода:\nАдрес = $ip:$port\nГлавная = $generateFirstBlock\nИндекс = $index\n\nБлоки:\n"
            }
        )
        for (block in blocks) {
            stringBuilder.append("Блок ${block.index}: ${block.hash}\n")
        }
        stringBuilder.append("\n")
        application.log.info(stringBuilder.toString())
    }

    private fun generateNextBlock(
        data: String,
        previousBlock: Block? = null,
        changeNonce: (Int) -> Int
    ): Block {
        val index = (previousBlock?.index ?: Configs.INITIAL_INDEX) + 1
        val previousHash = previousBlock?.hash ?: Configs.INITIAL_HASH

        var nonce = Random.nextInt()
        var hash = getHash(index, previousHash, data, nonce)
        while (!hash.endsWith(Configs.HASH_POSTFIX)) {
            nonce = changeNonce(nonce)
            hash = getHash(index, previousHash, data, nonce)
        }

        return Block(
            index = index,
            previousHash = previousHash,
            hash = hash,
            data = data,
            nonce = nonce
        )
    }
}