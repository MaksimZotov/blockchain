package com.maksimzotov.models

data class Node(
    val ip: String,
    val port: Int,
    val changeNonce: String,
    val generateFirstBlock: Boolean
) {
    val fullAddress get() = "$ip:$port"
}