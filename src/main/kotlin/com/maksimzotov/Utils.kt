package com.maksimzotov

import com.maksimzotov.models.Block
import kotlin.random.Random

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun getHash(index: Int, previousHash: String, data: String, nonce: Int) =
    "$index$previousHash$data${nonce.toAbsBinary()}".toHash()

fun checkHash(currentBlock: Block, previousBlock: Block?): Boolean {
    if (previousBlock == null) {
        return true
    }
    return getHash(
        index = currentBlock.index,
        previousHash = previousBlock.hash,
        data = currentBlock.data,
        nonce = currentBlock.nonce
    ) == currentBlock.hash
}

fun getInitialIndex() = 0

fun getInitialHash() = with(Configs) {
    "0".repeat(HASH_LENGTH - HASH_POSTFIX.length) + HASH_POSTFIX
}

fun generateNextBlock(
    data: String,
    previousBlock: Block? = null,
    changeNonce: (Int) -> Int
): Block {
    val index = (previousBlock?.index ?: getInitialIndex()) + 1
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
        nonce = nonce
    )
}