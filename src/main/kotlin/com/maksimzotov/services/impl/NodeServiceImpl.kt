package com.maksimzotov.services.impl

import com.maksimzotov.*
import com.maksimzotov.models.Block
import com.maksimzotov.models.Node
import com.maksimzotov.services.NeighbourNodesService
import com.maksimzotov.services.NodeService
import io.ktor.server.application.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class NodeServiceImpl(
    private val application: Application,
    private val neighbourNodesService: NeighbourNodesService,
    private val node: Node
) : NodeService {

    private companion object {
        const val INITIAL_INDEX = 0
    }

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

    @Volatile
    private var enableToGenerate = node.generateFirstBlock

    override suspend fun start() {
        while (true) {
            if (!enableToGenerate) {
                continue
            }

            val generatedBlock = generateNextBlock(
                data = getRandomString(Configs.DATA_LENGTH),
                previousBlock = blocks.lastOrNull(),
                changeNonce = changeNonceLambda
            )

            mutex.withLock {
                if (blocks.checkBlockIndexIsLargest(generatedBlock)) {
                    blocks.add(generatedBlock)
                }
            }

            try {
                neighbourNodesService.notifyAboutAddedBlock(generatedBlock)
            } catch (_: Exception) {
                mutex.withLock {
                    blocks.remove(generatedBlock)
                }
            }

            printCurrentState()
        }
    }

    override suspend fun onBlockAdded(block: Block): Unit = mutex.withLock {
        val checked = checkHash(
            currentBlock = block,
            previousBlock = blocks.lastOrNull()
        )
        if (checked) {
            blocks.add(block)
        } else if (blocks.checkBlockIndexIsLargest(block)) {
            try {
                blocks = neighbourNodesService.getCheckedBlocksWithMaxLength(blocks).toMutableList()
            } catch (_: Exception) { }
        }
        enableToGenerate = true
    }

    override suspend fun getBlocks() = mutex.withLock {
        blocks
    }

    private suspend fun printCurrentState() {
        val stringBuilder = StringBuilder(
            with(node) {
                "\n\nНода:\nАдрес = $ip:$port\nГлавная = $generateFirstBlock\n\n\nБлоки:\n"
            }
        )
        mutex.withLock {
            for (block in blocks) {
                stringBuilder.append("Блок от ${block.nodeFullAddress} c i = ${block.index}: ${block.hash}\n")
            }
        }
        stringBuilder.append("\n")
        application.log.info(stringBuilder.toString())
    }

    private fun generateNextBlock(
        data: String,
        previousBlock: Block? = null,
        changeNonce: (Int) -> Int
    ): Block {
        val index = (previousBlock?.index ?: INITIAL_INDEX) + 1
        val previousHash = previousBlock?.hash ?: getInitialHash()

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
            nonce = nonce,
            nodeFullAddress = node.fullAddress
        )
    }

    private fun List<Block>.checkBlockIndexIsLargest(block: Block) =
        block.index > (this.lastOrNull()?.index ?: INITIAL_INDEX)
}