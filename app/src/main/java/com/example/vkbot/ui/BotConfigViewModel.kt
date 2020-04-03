package com.example.vkbot.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.github.kittinunf.result.coroutines.getOrNull
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.coroutines.*
import tk.skeptick.vk.apiclient.VkApiClient
import tk.skeptick.vk.apiclient.VkApiUser
import tk.skeptick.vk.apiclient.execute
import kotlin.coroutines.CoroutineContext

class BotConfigViewModel : ViewModel(),
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.IO

    private val _validKey = MutableLiveData<String?>(null)

    val validKey: LiveData<String?> = _validKey

    val canStart: LiveData<Boolean> = Transformations.map(validKey) { it != null }

    fun submit(key: String) {
        launch { validate(key) }
    }

    private suspend fun validate(key: String) {
        val res = VkApiUser(
            VkApiClient(
                key,
                HttpClient(Android)
            )
        ).account.getProfileInfo()
            .execute().getOrNull()
        Log.d("BotConfig", res.toString())
        withContext(Dispatchers.Main) {
            _validKey.value = key.takeIf { res != null }
        }
    }
}