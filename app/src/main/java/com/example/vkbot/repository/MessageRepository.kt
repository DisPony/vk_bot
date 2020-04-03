package com.example.vkbot.repository

import com.example.vkbot.network.defaultClient
import com.github.kittinunf.result.coroutines.getOrNull
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import tk.skeptick.vk.apiclient.VkApiClient
import tk.skeptick.vk.apiclient.VkApiUser
import tk.skeptick.vk.apiclient.domain.models.Message
import tk.skeptick.vk.apiclient.execute
import tk.skeptick.vk.apiclient.methods.messages.LongPollHistoryResponse
import kotlin.random.Random

class MessageRepository(private val api: VkApiUser) {
    private fun updates(): Flow<LongPollHistoryResponse> = flow {
        val lp = api.messages.getLongPollServer(needPts = true).execute().get()
        var pts = lp.pts
        while (true) {
            val history = api.messages.getLongPollHistory(lp.ts, pts!!).execute().getOrNull()
            history?.let {
                emit(it)
                pts = it.newPts
            }
            delay(750 + Random.nextLong(0, 500))
        }
    }

    fun messages(): Flow<Message> = updates().flatMapConcat { it.messages.items.asFlow() }

    suspend fun send(peerId: Int, text: String) {
        api.messages.send(peerId, Random.nextInt(), text).execute()
    }
}

internal fun createMessageRepository(
    key: String,
    client: HttpClient = defaultClient
): MessageRepository {
    val apiClient = VkApiClient(key, client)
    return MessageRepository(
        VkApiUser(
            apiClient
        )
    )
}