package com.example.vkbot.repository

import com.github.kittinunf.result.coroutines.success
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import tk.skeptick.vk.apiclient.VkApiClient
import tk.skeptick.vk.apiclient.VkApiUser
import tk.skeptick.vk.apiclient.domain.models.Message
import tk.skeptick.vk.apiclient.execute
import tk.skeptick.vk.apiclient.methods.messages.LongPollHistoryResponse
import kotlin.random.Random

private const val DEFAULT_PERIOD = 1000L

class MessageRepository(private val api: VkApiUser, private val requestPeriod: Long) {
    private fun updates(): Flow<LongPollHistoryResponse> = flow {
        val lp = api.messages.getLongPollServer(needPts = true).execute().get()
        var pts = lp.pts
        while (true) {
            api.messages.getLongPollHistory(lp.ts, pts!!).execute().success {
                emit(it)
                pts = it.newPts
            }
            delay(requestPeriod)
        }
    }

    fun messages(): Flow<Message> = updates().flatMapConcat { it.messages.items.asFlow() }

    suspend fun send(peerId: Int, text: String) {
        api.messages.send(peerId, Random.nextInt(), text).execute()
    }
}

internal fun createMessageRepository(
    key: String,
    client: HttpClient = HttpClient(Android)
): MessageRepository {
    val apiClient = VkApiClient(key, client)
    return MessageRepository(
        VkApiUser(
            apiClient
        ), DEFAULT_PERIOD
    )
}