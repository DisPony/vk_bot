package com.example.vkbot.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.vkbot.R
import com.google.android.material.snackbar.Snackbar


class BotConfig : DialogFragment() {

    private val viewModel: BotConfigViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bot_config_fragment, container, false)
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
                    activity?.getSharedPreferences("bot", Context.MODE_PRIVATE)?.edit()?.run {
                        putString("key", key)
                        commit()
                    }
                    findNavController().popBackStack()
                }
            } else {
                Snackbar.make(view, "Ключ не валиден", Snackbar.LENGTH_SHORT).show()
            }
        })

        view.findViewById<Button>(R.id.button2).setOnClickListener {
            findNavController().popBackStack()
        }
    }

}


