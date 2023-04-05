package com.maksimzotov

import java.security.MessageDigest
import kotlin.math.abs

fun Int.toAbsBinary(): String =
    Integer.toBinaryString(abs(this))

fun String.toHash(): String {
    return MessageDigest
        .getInstance(Config.HASH_ALGORITHM)
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}