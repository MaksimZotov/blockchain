package com.maksimzotov

import com.maksimzotov.di.getKoinProjectModuleTest
import com.maksimzotov.models.Block
import com.maksimzotov.models.NeighbourNode
import com.maksimzotov.models.Node
import com.maksimzotov.services.impl.NodeServiceImpl
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ktor.ext.get
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class ApplicationTest {

    private inline fun test(
        crossinline block: Application.() -> Unit
    ) = testApplication {
        val node = Node(
            ip = "",
            port = 0,
            changeNonce = "",
            generateFirstBlock = false
        )
        val neighbourNodes = emptyList<NeighbourNode>()
        application {
            startKoin {
                modules(
                    getKoinProjectModuleTest(
                        node = node,
                        neighbourNodes = neighbourNodes
                    )
                )
            }
            block(this@application)
            stopKoin()
        }
    }

    @Test
    fun testCorrectAddedGenesisBlock() = test {
        val nodeService = get<NodeServiceImpl>()

        val genesisBlock = nodeService.generateNextBlock(
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
    fun testIncorrectAddedGenesisBlock() {
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
    fun testCorrectAddedSecondBlock() = test {
        val nodeService = get<NodeServiceImpl>()

        val genesisBlock = nodeService.generateNextBlock(
            data = getRandomString(Configs.DATA_LENGTH),
            previousBlock = null
        )

        assertNotNull(genesisBlock)

        val secondBlock = nodeService.generateNextBlock(
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
    fun testIncorrectAddedSecondBlock() = test {
        val nodeService = get<NodeServiceImpl>()

        val genesisBlock = nodeService.generateNextBlock(
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
}