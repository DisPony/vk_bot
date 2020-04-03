package com.example.vkbot.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.vkbot.R
import com.google.android.material.snackbar.Snackbar


const val BOT_PREFERENCES_NODE = "bot"
const val BOT_PREFERENCE_KEY = "key"

class BotConfigFragment : DialogFragment() {

    private val viewModel: BotConfigViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bot_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText = view.findViewById<EditText>(R.id.textView).apply {
            setText(viewModel.validKey.value ?: "")
        }

        val initKey: String? = viewModel.validKey.value

        view.findViewById<Button>(R.id.button).setOnClickListener {
            viewModel.submit(editText.text.toString())
        }

        viewModel.validKey.observe(viewLifecycleOwner, Observer { key ->
            if (key != null) {
                if (key != initKey) {
                    activity?.getSharedPreferences(BOT_PREFERENCES_NODE, Context.MODE_PRIVATE)?.edit()?.run {
                        putString(BOT_PREFERENCE_KEY, key)
                        commit()
                    }
                    findNavController().popBackStack()
                }
            } else {
                Snackbar.make(view, getString(R.string.invalid_key), Snackbar.LENGTH_SHORT).show()
            }
        })

        view.findViewById<Button>(R.id.button2).setOnClickListener {
            findNavController().popBackStack()
        }
    }

}


