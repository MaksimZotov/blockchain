package com.maksimzotov

data class Block(
    val index: Int,
    val previousHash: String,
    val hash: String,
    val data: String,
    val nonce: Int
)