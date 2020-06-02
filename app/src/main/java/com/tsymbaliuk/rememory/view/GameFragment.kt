package com.tsymbaliuk.rememory.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.tsymbaliuk.rememory.R
import com.tsymbaliuk.rememory.model.Card
import com.tsymbaliuk.rememory.view.adapter.GameAdapter
import com.tsymbaliuk.rememory.viewmodel.GameViewModel
import kotlinx.android.synthetic.main.game_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*
import kotlin.math.sqrt

class GameFragment : Fragment() {

    private val gameViewModel: GameViewModel by sharedViewModel()

    private lateinit var recyclerViewAdapter: GameAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.game_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("key")
            ?.observe(viewLifecycleOwner, Observer { result ->
                if (result == "restart") {
                    gameViewModel.onStartNewGame(nextRound = false)
                } else if (result == "next") {
                    gameViewModel.onStartNewGame(nextRound = true)
                }
            })

        gameViewModel.onStartNewGame(nextRound = true)

        setItemList()

        restart_iv.setOnClickListener {
            gameViewModel.onStartNewGame(nextRound = false)
        }

        back_iv.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_gameFragment_to_mainFragment)
        )

        gameViewModel.isWin.observe(viewLifecycleOwner, Observer { isWin ->
            if (isWin) onEndGame()
        })

        gameViewModel.isLose.observe(viewLifecycleOwner, Observer { isLose ->
            if (isLose) onEndGame()
        })

        gameViewModel.attempts.observe(viewLifecycleOwner, Observer { attempts ->
            val attemptsText = "Attempts: $attempts"
            attempts_tv.text = attemptsText
        })
        gameViewModel.cardSet.observe(viewLifecycleOwner, Observer { cardSet ->
            val cardArrayList = arrayListOf<Card>().apply { addAll(cardSet) }
            recyclerViewAdapter.updateData(cardArrayList)
        })
        gameViewModel.timeLeft.observe(viewLifecycleOwner, Observer { timeLeft ->
            val sec = timeLeft / 1000
            val minutes = ((sec) / 60).toInt()
            val seconds = ((sec) % 60).toInt()

            val timeLeftFormatted: String =
                java.lang.String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds)

            chronometer_tv.text = timeLeftFormatted
        })

    }

    private fun setItemList() {
        recyclerViewAdapter = GameAdapter(requireContext())

        recyclerViewAdapter.setClickListener { position ->
            gameViewModel.onCardClick(position)
        }

        val orientation =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                GridLayoutManager.VERTICAL
            } else {
                GridLayoutManager.HORIZONTAL
            }

        game_rv.apply {
            layoutManager = GridLayoutManager(
                activity,
               3,
                orientation,
                false
            )
            adapter = recyclerViewAdapter
        }

    }

    private fun onEndGame() {
        findNavController().navigate(R.id.action_game_fragment_to_gameOverDialogFragment)
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.onPauseGame()
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.onResumeGame()
    }

}