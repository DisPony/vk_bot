package com.example.vkbot.bot

import tk.skeptick.vk.apiclient.chatIdToPeerId
import tk.skeptick.vk.apiclient.domain.models.Message

interface DialogueResponder {
    val name: String
    val peerId: Int
    fun respondOrNull(message: Message): String?
}

class SimpleDialogueResponder(
    override val name: String,
    override val peerId: Int,
    private val filter: (Message) -> Boolean,
    private val extractor: (Message) -> String
) : DialogueResponder {

    override fun respondOrNull(message: Message): String? {
        return message.takeIf(filter)?.let(extractor)
    }
}

data class Group(
    val name: String,
    val peerId: Int,
    private val regex: Regex,
    private val extractor: (MatchResult) -> Int
) {

    fun extractNumberOrNull(message: Message): Int? {
        return regex.matchEntire(message.text)?.let { extractor(it) }
    }

}

class GroupAdapter(private val group: Group): DialogueResponder {

    override val name: String
        get() = group.name
    override val peerId: Int
        get() = group.peerId

    override fun respondOrNull(message: Message): String? {
        return group.extractNumberOrNull(message)?.let { "еду $it" }
    }

}

fun Group.asResponder(): DialogueResponder = GroupAdapter(this)

val dialogues: Map<Int, DialogueResponder> = mapOf(
    31.chatIdToPeerId to Group(
        name = "Иван грузчик",
        peerId = 31.chatIdToPeerId,
        regex = Regex("""Белгород, нужны: (\d) (.|\s)+"""),
        extractor = { match -> val (n) = match.destructured; n.toInt() }
    ).asResponder(),
    32.chatIdToPeerId to Group(
        name = "Александр грузчик",
        peerId = 32.chatIdToPeerId,
        regex = Regex("""Белгород, нужны: (\d) (.|\s)+"""),
        extractor = { match -> val (n) = match.destructured; n.toInt() }
    ).asResponder()
)

class Bot(private val responders: Map<Int, DialogueResponder>) : (Message) -> String? {
    override fun invoke(message: Message): String? {
        return responders[message.peerId]?.respondOrNull(message)
    }
}
