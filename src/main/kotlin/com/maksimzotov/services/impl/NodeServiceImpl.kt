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
    private var generationEnabled = node.generateFirstBlock

    @Volatile
    private var generationStopped = false

    @Volatile
    private var isActive = true

    override suspend fun start() {
        while (isActive) {
            if (!generationEnabled) {
                continue
            }

            val generatedBlock = generateNextBlock(
                data = getRandomString(Configs.DATA_LENGTH),
                previousBlock = blocks.lastOrNull()
            ) ?: continue

            val goToNextIteration = mutex.withLock {
                if (blocks.checkBlockIndexIsLargest(generatedBlock)) {
                    blocks.add(generatedBlock)
                    false
                } else {
                    true
                }
            }
            if (goToNextIteration) {
                continue
            }

            try {
                neighbourNodesService.notifyAboutAddedBlock(generatedBlock)
            } catch (_: Exception) {
                mutex.withLock {
                    blocks.remove(generatedBlock)
                }
            }

            logCurrentState()
        }
    }

    override suspend fun onBlockAdded(block: Block): Unit = mutex.withLock {
        val checked = checkBlocks(
            currentBlock = block,
            previousBlock = blocks.lastOrNull()
        )
        if (checked) {
            blocks.add(block)
            generationStopped = true
        } else if (blocks.checkBlockIndexIsLargest(block)) {
            application.log.info("Обновление всех блоков")
            try {
                blocks = neighbourNodesService.getCheckedBlocksWithMaxLength(blocks).toMutableList()
                generationStopped = true
            } catch (_: Exception) { }
        }
        generationEnabled = true
    }

    override suspend fun getBlocks() = mutex.withLock {
        blocks
    }

    override fun stop() {
        isActive = false
        generationEnabled = false
        generationStopped = true
    }

    fun generateNextBlock(
        data: String,
        previousBlock: Block?
    ): Block? {
        val index = previousBlock?.index?.plus(1) ?: Configs.GENESIS_BLOCK_INDEX
        val previousHash = previousBlock?.hash ?: getGenesisBlockPreviousHash()

        var nonce = changeNonceLambda(previousBlock?.nonce ?: Random.nextInt())
        var hash: String

        while (true) {
            if (generationStopped) {
                generationStopped = false
                return null
            }
            nonce = changeNonceLambda(nonce)
            hash = getHash(index, previousHash, data, nonce)
            if (hash.endsWith(Configs.HASH_POSTFIX)) {
                return Block(
                    index = index,
                    previousHash = previousHash,
                    hash = hash,
                    data = data,
                    nonce = nonce,
                    nodeFullAddress = node.fullAddress
                )
            }
        }
    }

    private suspend fun logCurrentState() {
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
}