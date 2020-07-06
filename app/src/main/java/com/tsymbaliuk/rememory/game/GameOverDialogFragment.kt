package com.tsymbaliuk.rememory.game

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.tsymbaliuk.rememory.R
import kotlinx.android.synthetic.main.game_over_dialog_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class GameOverDialogFragment : DialogFragment(), View.OnClickListener {

    private val gameViewModel: GameViewModelImpl by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.game_over_dialog_fragment, container, false)

        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (gameViewModel.timeLeft.value != null) {
            val sec = gameViewModel.timeLeft.value!! / 1000
            val minutes = (sec / 60).toInt()
            val seconds = (sec % 60).toInt()

            val currentGameTimeFormatted: String =
                java.lang.String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

            time_value.text = currentGameTimeFormatted
        }
        attempts_value.text = gameViewModel.attempts.value.toString()
        total_value.text = gameViewModel.currentGame.score.toString()

        restart_button.setOnClickListener(this)
        next_button.setOnClickListener(this)
        home_button.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        when (view) {
            restart_button -> {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("key", "restart")
                findNavController().popBackStack()
            }
            next_button -> {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("key", "next")
                findNavController().popBackStack()
            }
            home_button -> findNavController().navigate(R.id.action_gameOverDialogFragment_to_main_fragment)
        }
    }
}