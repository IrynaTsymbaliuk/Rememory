package com.tsymbaliuk.rememory.viewmodel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.*
import com.tsymbaliuk.rememory.model.*
import com.tsymbaliuk.rememory.model.database.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val MILLISECONDS_IN_SECOND: Long = 1000
private const val TIME_TO_REMEMBER: Long = 1000
private const val TIME_TO_CARD_FLIP_ANIM: Long = 300

class GameViewModel(
    private val playGamesServicesManager: PlayGamesServicesManager,
    private val repository: AppRepository
) : ViewModel() {

    val allLevels: LiveData<ArrayList<Level>> = repository.allLevels
    val allGameResults: LiveData<List<GameResult>> = repository.allGameResults

    lateinit var currentGame: GameResult
    private var timer: CountDownTimer? = null

    private var _attempts = MutableLiveData<Int>().apply { value = 0 }
    val attempts: LiveData<Int>
        get() = _attempts

    private var _cardSet = MutableLiveData<List<Card>>()
    val cardSet: LiveData<List<Card>>
        get() = _cardSet

    private var _isLose = MutableLiveData<Boolean>().apply { value = false }
    val isLose: LiveData<Boolean>
        get() = _isLose

    private var _isWin = MutableLiveData<Boolean>().apply { value = false }
    val isWin: LiveData<Boolean>
        get() = _isWin

    private var _timeLeft = MutableLiveData<Long>()
    val timeLeft: LiveData<Long>
        get() = _timeLeft

    fun onStartNewGame(nextRound: Boolean) {
        if (nextRound) {
            Log.e("Game", "next")
            createNextGame()
        } else {
            Log.e("Game", "repeat")
            copyCurrentGame()
        }
        setGameParams()
        viewModelScope.launch(Dispatchers.Default) {
            delay(currentGame.levelIndex * allLevels.value!![currentGame.levelIndex].rounds!![currentGame.roundIndex].time!! + TIME_TO_REMEMBER)
            launch(Dispatchers.Main) {
                hideCards()
                startTimer()
            }
        }
    }

    private fun createNextGame() {
        var level = 0
        var round = 0
        if (!allGameResults.value.isNullOrEmpty()) {
            level = allGameResults.value!!.maxBy { it.levelIndex }!!.levelIndex
            round = if (allGameResults.value!!.filter { it.levelIndex == level }
                    .maxBy { it.roundIndex }!!.roundIndex + 1 == 3) {
                level++
                0
            } else {
                allGameResults.value!!.filter { it.levelIndex == level }
                    .maxBy { it.roundIndex }!!.roundIndex + 1
            }
        }
        currentGame = GameResult(
            gameResultId = level.toString() + round.toString(),
            levelIndex = level,
            roundIndex = round,
            score = 0,
            time = 0L,
            attempts = 0
        )
        Log.e(
            "Game",
            "id ${currentGame.gameResultId} level ${currentGame.levelIndex} round ${currentGame.roundIndex}"
        )
    }

    private fun copyCurrentGame() {
        currentGame.score = 0
        currentGame.time = 0
        currentGame.attempts = 0
    }

    private fun setGameParams() {
        _attempts.value = 0
        _cardSet.value = getRandomCardSet()
        _isLose.value = false
        _isWin.value = false

        if (!allLevels.value.isNullOrEmpty() && allLevels.value!![currentGame.levelIndex].rounds != null) {
            _timeLeft.value =
                allLevels.value!![currentGame.levelIndex].rounds!![currentGame.roundIndex].time!! * MILLISECONDS_IN_SECOND
        }
    }

    private fun startTimer() {
        if (timer == null && _timeLeft.value != null) {
            timer = CustomCountDownTimer(
                (_timeLeft.value!!),
                MILLISECONDS_IN_SECOND
            ).start()
        }
    }

    private fun getRandomCardSet(): List<Card> {
        val cardList = arrayListOf<Card>()
        if (!allLevels.value.isNullOrEmpty()) {
            val drawablePool =
                arrayListOf<String>().apply {
                    addAll(allLevels.value!![currentGame.levelIndex].itemDrawables!!)
                }
            for (set in 1..allLevels.value!![currentGame.levelIndex].rounds!![currentGame.roundIndex].numberOfSets!!) {
                if (drawablePool.size != 0) {
                    val random = Random.nextInt(0, drawablePool.size)
                    for (item in 1..allLevels.value!![currentGame.levelIndex].rounds!![currentGame.roundIndex].numberOfItemsInSet!!) {
                        cardList.add(Card(drawablePool[random], CardState.OPEN))
                    }
                    drawablePool.removeAt(random)
                }
            }
            cardList.shuffle()
        }
        return cardList
    }

    private fun hideCards() {
        _cardSet.value?.forEachIndexed { index, card ->
            card.state = CardState.CLOSE
        }
        _cardSet.value = _cardSet.value
    }

    fun onPauseGame() {
        pauseTimer()
    }

    private fun pauseTimer() {
        timer?.cancel()
        timer = null
    }

    fun onResumeGame() {
        startTimer()
    }

    fun onCardClick(position: Int) {
        _attempts.value = _attempts.value?.plus(1)
        if (isFirstInSet()) {
            openCard(position)
        } else if (isSameInSet(position)) {
            openCard(position)
            if (isLastInSet()) discloseAllOpenCards()
            if (isLastInGame()) onEndGame()
        } else if (isDifferentInSet(position)) {
            openCard(position)
            closeAllOpenCards()
        }
    }

    private fun isFirstInSet(): Boolean {
        return if (_cardSet.value != null) {
            _cardSet.value!!.none { it.state == CardState.OPEN }
        } else false
    }

    private fun isSameInSet(position: Int): Boolean {
        return if (_cardSet.value != null) {
            _cardSet.value!!.filter { it.state == CardState.OPEN }[0].drawableUri == _cardSet.value!![position].drawableUri
        } else false
    }

    private fun isLastInSet(): Boolean {
        return if (_cardSet.value != null && allLevels.value != null) {
            _cardSet.value!!.filter { it.state == CardState.OPEN }.size == allLevels.value!![currentGame.levelIndex].rounds!![currentGame.roundIndex].numberOfItemsInSet
        } else false
    }

    private fun isLastInGame(): Boolean {
        return if (_cardSet.value != null) {
            _cardSet.value!!.none { it.state == CardState.CLOSE }
        } else false
    }

    private fun isDifferentInSet(position: Int): Boolean {
        return if (_cardSet.value != null) {
            _cardSet.value!!.filter { it.state == CardState.OPEN }[0].drawableUri != _cardSet.value!![position].drawableUri
        } else false
    }

    private fun openCard(position: Int) {
        if (_cardSet.value != null) {
            _cardSet.value!![position].state = CardState.OPEN
            _cardSet.value = _cardSet.value
        }
    }

    private fun closeAllOpenCards() {
        viewModelScope.launch(Dispatchers.Default) {
            delay(TIME_TO_CARD_FLIP_ANIM)
            launch(Dispatchers.Main) {
                if (_cardSet.value != null) {
                    _cardSet.value!!.filter { it.state == CardState.OPEN }.forEach { card ->
                        card.state = CardState.CLOSE
                    }
                    _cardSet.value = _cardSet.value
                }
            }
        }
    }

    private fun discloseAllOpenCards() {
        viewModelScope.launch(Dispatchers.Default) {
            delay(TIME_TO_CARD_FLIP_ANIM)
            launch(Dispatchers.Main) {
                if (_cardSet.value != null) {
                    _cardSet.value!!.filter { it.state == CardState.OPEN }.forEach { card ->
                        card.state = CardState.DISCLOSE
                    }
                    _cardSet.value = _cardSet.value
                }
            }
        }
    }

    private fun onEndGame() {
        pauseTimer()
        currentGame.score = calculateScore()
        if (_timeLeft.value == 0L) onLose() else onWin()
        currentGame.time = calculateTime()
        currentGame.attempts = _attempts.value!!
        saveGame()
    }

    private fun onLose() {
        _isLose.value = true
    }

    private fun onWin() {
        playGamesServicesManager.submitScore(currentGame.score)
        _isWin.value = true
    }

    private fun calculateScore(): Long {
        return if (allLevels.value != null && _timeLeft.value != null) {
            allLevels.value!![currentGame.levelIndex].rounds!![currentGame.roundIndex].numberOfSets!! *
                    allLevels.value!![currentGame.levelIndex].rounds!![currentGame.roundIndex].numberOfItemsInSet!! -
                    _attempts.value!! +
                    _timeLeft.value!! / MILLISECONDS_IN_SECOND
        } else 0
    }

    private fun calculateTime(): Long {
        return if (allLevels.value != null && _timeLeft.value != null) {
            allLevels.value!![currentGame.levelIndex].rounds!![currentGame.roundIndex].time!! - _timeLeft.value!!
        } else 0
    }

    private fun saveGame() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertGameResult(currentGame)
            Log.e("Game", "GameResult saving")
        }
    }

    inner class CustomCountDownTimer(millisLeft: Long, interval: Long) :
        CountDownTimer(millisLeft, interval) {
        override fun onTick(millisUntilFinished: Long) {
            _timeLeft.value = millisUntilFinished
        }

        override fun onFinish() {
            onEndGame()
        }
    }

}