package com.maksimzotov

object Config {
    const val FIRST_NODE_ID = "1"
    const val SECOND_NODE_ID = "2"
    const val THIRD_NODE_ID = "3"

    const val HASH_ALGORITHM = "SHA-256"
    const val HASH_POSTFIX = "0000"
    const val HASH_LENGTH = 64
    const val DATA_LENGTH = 256

    const val CHANGE_NONCE_RANDOM = "CHANGE_NONCE_RANDOM"
    const val CHANGE_NONCE_INCREMENT = "CHANGE_NONCE_INCREMENT"
    const val CHANGE_NONCE_DECREMENT = "CHANGE_NONCE_DECREMENT"

    const val STUB_DELAY = 1000L
    const val STUB_NODE_ATTEMPTS_COUNT = 3

    val nodesIds = listOf(FIRST_NODE_ID, SECOND_NODE_ID, THIRD_NODE_ID)
}