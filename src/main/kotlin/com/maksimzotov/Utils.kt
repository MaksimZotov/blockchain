package com.maksimzotov

import com.maksimzotov.models.Block

fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

fun checkHash(currentBlock: Block, previousBlock: Block?): Boolean {
    if (previousBlock == null || currentBlock.index == 1) {
        return true
    }
    return getHash(
        index = currentBlock.index,
        previousHash = previousBlock.hash,
        data = currentBlock.data,
        nonce = currentBlock.nonce
    ) == currentBlock.hash
}

fun getHash(index: Int, previousHash: String, data: String, nonce: Int) =
    "$index$previousHash$data${nonce.toAbsBinary()}".toHash()

fun getInitialHash() = with(Configs) {
    "0".repeat(HASH_LENGTH - HASH_POSTFIX.length) + HASH_POSTFIX
}