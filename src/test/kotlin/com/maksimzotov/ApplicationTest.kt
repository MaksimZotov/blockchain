package com.maksimzotov

import com.maksimzotov.models.Block
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class ApplicationTest {

    private companion object {
        const val DELAY = 10_000L
    }

    @Test
    fun testCorrectGenesisBlock() = test { _, _, _, _, _, _, nodeServiceImpl ->
        val genesisBlock = nodeServiceImpl.generateNextBlock(
            data = getRandomString(Configs.DATA_LENGTH),
            previousBlock = null
        )

        assertNotNull(genesisBlock)

        assert(
            checkBlocks(
                currentBlock = genesisBlock,
                previousBlock = null
            )
        )
    }

    @Test
    fun testIncorrectGenesisBlock() = test { _, _, _, _, _, _, _ ->
        assertFalse(
            checkBlocks(
                currentBlock = Block(
                    index = Configs.GENESIS_BLOCK_INDEX,
                    previousHash = getGenesisBlockPreviousHash(),
                    hash = getGenesisBlockPreviousHash(),
                    data = getRandomString(Configs.DATA_LENGTH),
                    nonce = 0,
                    nodeFullAddress = ""
                ),
                previousBlock = null
            )
        )
    }

    @Test
    fun testCorrectSecondBlock() = test { _, _, _, _, _, _, nodeServiceImpl ->
        val genesisBlock = nodeServiceImpl.generateNextBlock(
            data = getRandomString(Configs.DATA_LENGTH),
            previousBlock = null
        )

        assertNotNull(genesisBlock)

        val secondBlock = nodeServiceImpl.generateNextBlock(
            data = getRandomString(Configs.DATA_LENGTH),
            previousBlock = genesisBlock
        )

        assertNotNull(secondBlock)

        assert(
            checkBlocks(
                currentBlock = secondBlock,
                previousBlock = genesisBlock
            )
        )
    }

    @Test
    fun testIncorrectSecondBlock() = test { _, _, _, _, _, _, nodeServiceImpl ->
        val genesisBlock = nodeServiceImpl.generateNextBlock(
            data = getRandomString(Configs.DATA_LENGTH),
            previousBlock = null
        )

        assertNotNull(genesisBlock)

        val secondBlock = Block(
            index = Configs.GENESIS_BLOCK_INDEX,
            previousHash = getGenesisBlockPreviousHash(),
            hash = getGenesisBlockPreviousHash(),
            data = getRandomString(Configs.DATA_LENGTH),
            nonce = 0,
            nodeFullAddress = ""
        )

        assertNotNull(secondBlock)

        assertFalse(
            checkBlocks(
                currentBlock = secondBlock,
                previousBlock = genesisBlock
            )
        )
    }

    @Test
    fun testCorrectBlocksList() = test { _, _, _, _, _, _, nodeServiceImpl ->
        val blocks = mutableListOf<Block>()
        repeat(3) {
            val next = nodeServiceImpl.generateNextBlock(
                data = getRandomString(Configs.DATA_LENGTH),
                previousBlock = blocks.lastOrNull()
            )
            assertNotNull(next)
            blocks.add(next)
        }
        assert(checkBlocksHashes(blocks))
    }

    @Test
    fun testIncorrectBlocksList() = test { _, _, _, _, _, _, nodeServiceImpl ->
        val blocks = mutableListOf<Block>()
        repeat(3) {
            val next = nodeServiceImpl.generateNextBlock(
                data = getRandomString(Configs.DATA_LENGTH),
                previousBlock = blocks.lastOrNull()
            )
            assertNotNull(next)
            blocks.add(next)
        }
        val invalidBlocks = blocks.mapIndexed { index, block ->
            if (index != 1) {
                block
            } else {
                block.copy(
                    hash = "Random"
                )
            }
        }
        assertFalse(checkBlocksHashes(invalidBlocks))
    }

    @Test
    fun testNotificationsAboutAddedBlocks() = test { _, _, _, nodeService1, nodeService2, nodeService3, _ ->
        CoroutineScope(Dispatchers.IO).launch {
            nodeService1.start()
        }

        runBlocking {
            delay(DELAY)

            nodeService1.stop()

            val blocksInNodeService1 = nodeService1.getBlocks().toList()
            val blocksInNodeService2 = nodeService2.getBlocks().toList()
            val blocksInNodeService3 = nodeService3.getBlocks().toList()

            val blocksWithMinLength = listOf(
                blocksInNodeService1,
                blocksInNodeService2,
                blocksInNodeService3
            ).minBy { blocks ->
                blocks.size
            }

            for (i in blocksWithMinLength.indices) {
                assert(
                    blocksWithMinLength[i] == blocksInNodeService1[i] &&
                    blocksWithMinLength[i] == blocksInNodeService2[i] &&
                    blocksWithMinLength[i] == blocksInNodeService3[i]
                )
            }
        }
    }

    @Test
    fun testBlocksConsistency() = test { _, _, _, nodeService1, nodeService2, nodeService3, _ ->
        CoroutineScope(Dispatchers.IO).launch {
            nodeService1.start()
        }
        CoroutineScope(Dispatchers.IO).launch {
            nodeService2.start()
        }
        CoroutineScope(Dispatchers.IO).launch {
            nodeService3.start()
        }

        runBlocking {
            delay(DELAY)

            nodeService1.stop()
            nodeService2.stop()
            nodeService3.stop()

            val blocksInNodeService1 = nodeService1.getBlocks().dropLastWhile { it.nodeFullAddress.startsWith(NODE_1_IP) }
            val blocksInNodeService2 = nodeService2.getBlocks().dropLastWhile { it.nodeFullAddress.startsWith(NODE_2_IP) }
            val blocksInNodeService3 = nodeService3.getBlocks().dropLastWhile { it.nodeFullAddress.startsWith(NODE_3_IP) }

            val blocksWithMinLength = listOf(
                blocksInNodeService1,
                blocksInNodeService2,
                blocksInNodeService3
            ).minBy { blocks ->
                blocks.size
            }

            for (i in blocksWithMinLength.indices) {
                assert(
                    blocksWithMinLength[i] == blocksInNodeService1[i] &&
                    blocksWithMinLength[i] == blocksInNodeService2[i] &&
                    blocksWithMinLength[i] == blocksInNodeService3[i]
                )
            }
        }
    }

    @Test
    fun testMinority() = test { neighbourNodeService1, _, _, nodeService1, nodeService2, _, nodeServiceImpl ->
        val genesisBlock = nodeServiceImpl.generateNextBlock(
            data = getRandomString(Configs.DATA_LENGTH),
            previousBlock = null
        )

        assertNotNull(genesisBlock)

        val secondBlock = nodeServiceImpl.generateNextBlock(
            data = getRandomString(Configs.DATA_LENGTH),
            previousBlock = genesisBlock
        )

        assertNotNull(secondBlock)

        val resultList = listOf(genesisBlock, secondBlock)

        runBlocking {
            neighbourNodeService1.notifyAboutAddedBlock(genesisBlock)
            neighbourNodeService1.notifyAboutAddedBlock(secondBlock)

            assert(nodeService1.getBlocks().isEmpty())
            assert(nodeService2.getBlocks() == resultList)
            nodeService1.onBlockAdded(secondBlock)
            assert(nodeService1.getBlocks() == resultList)
        }
    }

    @Test
    fun testMinorityWithIncorrectBlocksFromNeighbour() = test { neighbourNodeService1, _, _, _, nodeService2, nodeService3, nodeServiceImpl ->
        val blocks = mutableListOf<Block>()

        repeat(3) {
            val next = nodeServiceImpl.generateNextBlock(
                data = getRandomString(Configs.DATA_LENGTH),
                previousBlock = blocks.lastOrNull()
            )
            assertNotNull(next)
            blocks.add(next)
        }

        val invalidBlocks = blocks.mapIndexed { index, block ->
            if (index != 2) {
                block
            } else {
                block.copy(
                    hash = "Random"
                )
            }
        }

        val resultList = blocks.dropLast(1)

        runBlocking {
            neighbourNodeService1.notifyAboutAddedBlock(invalidBlocks[0])
            neighbourNodeService1.notifyAboutAddedBlock(invalidBlocks[1])
            neighbourNodeService1.notifyAboutAddedBlock(invalidBlocks[2])

            assert(nodeService2.getBlocks() == resultList)
            assert(nodeService3.getBlocks() == resultList)
        }
    }
}