package com.maksimzotov

object Configs {
    const val HASH_ALGORITHM = "SHA-256"
    const val HASH_POSTFIX = "0000"
    const val HASH_LENGTH = 64
    const val DATA_LENGTH = 256

    const val FAILED_REQUEST_DELAY = 5000L

    const val REQUEST_GET_BLOCKS = "/blocks"
    const val REQUEST_CHECK_ADDED_BLOCK = "/check_added_block"
}