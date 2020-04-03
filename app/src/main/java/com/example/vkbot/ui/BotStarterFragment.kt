package com.example.vkbot.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.vkbot.R
import com.example.vkbot.service.BotService


class BotStarterFragment : Fragment() {

    private val viewModel: BotConfigViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_manager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.edit_key).apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_botManager_to_botConfig)
            }
        }

        val startButton = view.findViewById<Button>(R.id.start_service).apply {
            setOnClickListener {
                activity?.let {
                    val key = viewModel.validKey.value ?: error("No key")
                    BotService.launch(it, key)
                } ?: Log.d("BotManager", "no activity")
            }
        }

        viewModel.canStart.observe(viewLifecycleOwner, Observer {
            startButton.isEnabled = it
        })

        activity?.getSharedPreferences(BOT_PREFERENCES_NODE, Context.MODE_PRIVATE)
            ?.run { getString(BOT_PREFERENCE_KEY, null) }
            ?.let { viewModel.submit(it) }
            ?: findNavController().navigate(R.id.action_botManager_to_botConfig)
    }

}
