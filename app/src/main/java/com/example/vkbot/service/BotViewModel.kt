package com.example.vkbot.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vkbot.repository.createMessageRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import tk.skeptick.vk.apiclient.chatIdToPeerId
import tk.skeptick.vk.apiclient.domain.models.Message
import java.util.*
import kotlin.coroutines.CoroutineContext


class BotViewModel(key: String, private val bot: (Message) -> String?) : ViewModel(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    private val messageRepository = createMessageRepository(key)

    private val _isStarted = MutableLiveData<Boolean>(false)
    val isStarted: LiveData<Boolean> = _isStarted

    private val _newJobEvent = MutableLiveData<String>()
    val newJobEvent: LiveData<String> = _newJobEvent

    fun onStart() {
        _isStarted.value = true
        launch {
            messageRepository.messages()
                .onEach { respond(it) }
                .collect()
        }
    }

    private suspend fun respond(message: Message) {
        val response = bot(message)
        if (response != null) {
            messageRepository.send(message.peerId, response)
            withContext(Dispatchers.Main) {
                _newJobEvent.value = message.text
            }
        }
    }

    fun onStop() {
        _isStarted.value = false
        job.cancelChildren()
    }

    override fun onCleared() {
        super.onCleared()
        onStop()
    }
}
