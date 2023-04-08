package com.maksimzotov

data class CheckAddedBlockResponse(
    val blocks: List<Block>?
) {
    val checked get() = blocks == null
    val lastBlock get() = blocks?.lastOrNull()
}