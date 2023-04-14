package com.maksimzotov

import com.maksimzotov.models.Block

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun checkBlocks(currentBlock: Block, previousBlock: Block?): Boolean {
    if (previousBlock == null) {
        return currentBlock.index == Configs.GENESIS_BLOCK_INDEX &&
               currentBlock.previousHash == getGenesisBlockPreviousHash() &&
               currentBlock.hash == with (currentBlock) { getHash(index, previousHash, data, nonce) }
    }
    return getHash(
        index = currentBlock.index,
        previousHash = previousBlock.hash,
        data = currentBlock.data,
        nonce = currentBlock.nonce
    ) == currentBlock.hash
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

fun getHash(index: Int, previousHash: String, data: String, nonce: Int) =
    "$index$previousHash$data${nonce.toAbsBinary()}".toHash()

fun getGenesisBlockPreviousHash() = with(Configs) {
    "0".repeat(HASH_LENGTH - HASH_POSTFIX.length) + HASH_POSTFIX
}

fun List<Block>.checkBlockIndexIsLargest(block: Block): Boolean {
    val lastBlock = this.lastOrNull() ?: return block.index >= Configs.GENESIS_BLOCK_INDEX
    return block.index > lastBlock.index
}