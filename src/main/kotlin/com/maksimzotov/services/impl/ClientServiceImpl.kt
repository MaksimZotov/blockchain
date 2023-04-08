package com.maksimzotov.services.impl

import com.maksimzotov.Configs
import com.maksimzotov.models.Block
import com.maksimzotov.models.CheckAddedBlockResponse
import com.maksimzotov.services.ClientService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ClientServiceImpl(
    private val httpClient: HttpClient
) : ClientService {

    override suspend fun getBlocks(fullAddress: String) =
        httpClient.request("$fullAddress${Configs.REQUEST_GET_BLOCKS}")
            .body<List<Block>>()

    override suspend fun checkAddedBlock(fullAddress: String, block: Block) =
        httpClient.request("$fullAddress${Configs.REQUEST_CHECK_ADDED_BLOCK}") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            setBody(block)
        }.body<CheckAddedBlockResponse>()
}