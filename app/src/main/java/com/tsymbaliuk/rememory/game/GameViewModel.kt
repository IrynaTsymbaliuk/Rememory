package com.tsymbaliuk.rememory.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tsymbaliuk.domain.level.model.LevelModel
import kotlinx.coroutines.flow.Flow

abstract class GameViewModel : ViewModel() {

    abstract val allLevels: LiveData<List<LevelModel>>
    abstract val attempts: LiveData<Int>
    abstract val cardSet: LiveData<List<Card>>
    abstract val isLose: LiveData<Boolean>
    abstract val isWin: LiveData<Boolean>
    abstract val timeLeft: LiveData<Long>

    abstract fun onStartNewGame(nextRound: Boolean)
    abstract fun onPauseGame()
    abstract fun onResumeGame()
    abstract fun onCardClick(position: Int)

}