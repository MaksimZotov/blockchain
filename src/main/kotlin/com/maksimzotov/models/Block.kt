package com.maksimzotov.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Block(
    val index: Int,
    @SerialName("previous_hash")
    val previousHash: String,
    val hash: String,
    val data: String,
    val nonce: Int
)