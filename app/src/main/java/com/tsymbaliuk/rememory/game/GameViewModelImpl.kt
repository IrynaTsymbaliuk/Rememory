package com.tsymbaliuk.rememory.game

import android.os.CountDownTimer
import androidx.lifecycle.*
import com.tsymbaliuk.domain.gameResult.model.GameResultModel
import com.tsymbaliuk.domain.gameResult.usecase.GameResultUseCase
import com.tsymbaliuk.domain.gameResult.usecase.GameResultUseCaseImpl
import com.tsymbaliuk.domain.level.model.LevelModel
import com.tsymbaliuk.domain.level.usecase.LevelUseCase
import com.tsymbaliuk.domain.level.usecase.LevelUseCaseImpl
import com.tsymbaliuk.rememory.user.PlayGamesServicesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val MILLISECONDS_IN_SECOND: Long = 1000
private const val TIME_TO_REMEMBER: Long = 1000
private const val TIME_TO_CARD_FLIP_ANIM: Long = 300

class GameViewModelImpl(
    private val playGamesServicesManager: PlayGamesServicesManager,
    private val gameResultUseCase: GameResultUseCase,
    private val levelUseCase: LevelUseCase
): GameViewModel() {

    override val allLevels: LiveData<List<LevelModel>> = levelUseCase.getAll().asLiveData()
    private val allGameResults = MutableLiveData<ArrayList<com.tsymbaliuk.domain.gameResult.model.GameResultModel>>().apply { value = arrayListOf<com.tsymbaliuk.domain.gameResult.model.GameResultModel>() }

    private var timer: CountDownTimer? = null
    lateinit var currentGame: com.tsymbaliuk.domain.gameResult.model.GameResultModel

    override val attempts = MutableLiveData<Int>().apply { value = 0 }
    override val cardSet = MutableLiveData<List<Card>>()
    override val isLose  = MutableLiveData<Boolean>().apply { value = false }
    override val isWin = MutableLiveData<Boolean>().apply { value = false }
    override val timeLeft = MutableLiveData<Long>()

    override fun onStartNewGame(nextRound: Boolean) {
        if (nextRound) {
            createNextGame()
        } else {
            copyCurrentGame()
        }
        setGameParams()
        viewModelScope.launch(Dispatchers.Default) {
            var time = TIME_TO_REMEMBER
            if (allLevels.value != null && allLevels.value!!.get(currentGame.levelIndex).roundList != null) {
                time = TIME_TO_REMEMBER + (currentGame.levelIndex * allLevels.value!![currentGame.levelIndex].roundList!![currentGame.roundIndex].time).toLong()
            }
            delay(time)
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
        currentGame = GameResultModel(
            gameResultId = level.toString() + round.toString(),
            levelIndex = level,
            roundIndex = round,
            score = 0,
            time = 0L,
            attempts = 0
        )
    }

    private fun copyCurrentGame() {
        currentGame.score = 0
        currentGame.time = 0
        currentGame.attempts = 0
    }

    private fun setGameParams() {
        attempts.value = 0
        cardSet.value = getRandomCardSet()
        isLose.value = false
        isWin.value = false

        if (!allLevels.value.isNullOrEmpty() && allLevels.value!![currentGame.levelIndex].roundList != null) {
            timeLeft.value =
                allLevels.value!![currentGame.levelIndex].roundList!![currentGame.roundIndex].time * MILLISECONDS_IN_SECOND
        }
    }

    private fun startTimer() {
        if (timer == null && timeLeft.value != null) {
            timer = CustomCountDownTimer(
                (timeLeft.value!!),
                MILLISECONDS_IN_SECOND
            ).start()
        }
    }

    private fun getRandomCardSet(): List<Card> {
        val cardList = arrayListOf<Card>()
        if (!allLevels.value.isNullOrEmpty()) {
            val drawablePool =
                arrayListOf<String>().apply {
                    addAll(allLevels.value!![currentGame.levelIndex].itemUris!!)
                }
            for (set in 1..allLevels.value!![currentGame.levelIndex].roundList!![currentGame.roundIndex].numberOfSets) {
                if (drawablePool.size != 0) {
                    val random = Random.nextInt(0, drawablePool.size)
                    for (item in 1..allLevels.value!![currentGame.levelIndex].roundList!![currentGame.roundIndex].numberOfItemsInSet) {
                        cardList.add(
                            Card(
                                drawablePool[random],
                                CardState.OPEN
                            )
                        )
                    }
                    drawablePool.removeAt(random)
                }
            }
            cardList.shuffle()
        }
        return cardList
    }

    private fun hideCards() {
        cardSet.value?.forEachIndexed { index, card ->
            card.state = CardState.CLOSE
        }
        cardSet.value = cardSet.value
    }

    override fun onPauseGame() {
        pauseTimer()
    }

    private fun pauseTimer() {
        timer?.cancel()
        timer = null
    }

    override fun onResumeGame() {
        startTimer()
    }

    override fun onCardClick(position: Int) {
        attempts.value = attempts.value?.plus(1)
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
        return if (cardSet.value != null) {
            cardSet.value!!.none { it.state == CardState.OPEN }
        } else false
    }

    private fun isSameInSet(position: Int): Boolean {
        return if (cardSet.value != null) {
            cardSet.value!!.filter { it.state == CardState.OPEN }[0].drawableUri == cardSet.value!![position].drawableUri
        } else false
    }

    private fun isLastInSet(): Boolean {
        return if (cardSet.value != null && allLevels.value != null) {
            cardSet.value!!.filter { it.state == CardState.OPEN }.size == allLevels.value!![currentGame.levelIndex].roundList!![currentGame.roundIndex].numberOfItemsInSet
        } else false
    }

    private fun isLastInGame(): Boolean {
        return if (cardSet.value != null) {
            cardSet.value!!.none { it.state == CardState.CLOSE }
        } else false
    }

    private fun isDifferentInSet(position: Int): Boolean {
        return if (cardSet.value != null) {
            cardSet.value!!.filter { it.state == CardState.OPEN }[0].drawableUri != cardSet.value!![position].drawableUri
        } else false
    }

    private fun openCard(position: Int) {
        if (cardSet.value != null) {
            cardSet.value!![position].state = CardState.OPEN
            cardSet.value = cardSet.value
        }
    }

    private fun closeAllOpenCards() {
        viewModelScope.launch(Dispatchers.Default) {
            delay(TIME_TO_CARD_FLIP_ANIM)
            launch(Dispatchers.Main) {
                if (cardSet.value != null) {
                    cardSet.value!!.filter { it.state == CardState.OPEN }.forEach { card ->
                        card.state = CardState.CLOSE
                    }
                    cardSet.value = cardSet.value
                }
            }
        }
    }

    private fun discloseAllOpenCards() {
        viewModelScope.launch(Dispatchers.Default) {
            delay(TIME_TO_CARD_FLIP_ANIM)
            launch(Dispatchers.Main) {
                if (cardSet.value != null) {
                    cardSet.value!!.filter { it.state == CardState.OPEN }.forEach { card ->
                        card.state = CardState.DISCLOSE
                    }
                    cardSet.value = cardSet.value
                }
            }
        }
    }

    private fun onEndGame() {
        pauseTimer()
        currentGame.score = calculateScore()
        if (timeLeft.value == 0L) onLose() else onWin()
        currentGame.time = calculateTime()
        currentGame.attempts = attempts.value!!
        saveGame()
    }

    private fun onLose() {
        isLose.value = true
    }

    private fun onWin() {
        playGamesServicesManager.submitScore(currentGame.score)
        isWin.value = true
    }

    private fun calculateScore(): Long {
        return if (allLevels.value != null && timeLeft.value != null) {
            allLevels.value!![currentGame.levelIndex].roundList!![currentGame.roundIndex].numberOfSets *
                    allLevels.value!![currentGame.levelIndex].roundList!![currentGame.roundIndex].numberOfItemsInSet -
                    attempts.value!! +
                    timeLeft.value!! / MILLISECONDS_IN_SECOND
        } else 0
    }

    private fun calculateTime(): Long {
        return if (allLevels.value != null && timeLeft.value != null) {
            allLevels.value!![currentGame.levelIndex].roundList!![currentGame.roundIndex].time - timeLeft.value!!
        } else 0
    }

   private fun saveGame() {
        allGameResults.value?.add(currentGame)
        viewModelScope.launch(Dispatchers.IO) {
            gameResultUseCase.saveGameResult(currentGame)
        }
    }

    inner class CustomCountDownTimer(millisLeft: Long, interval: Long) :
        CountDownTimer(millisLeft, interval) {
        override fun onTick(millisUntilFinished: Long) {
            timeLeft.value = millisUntilFinished
        }

        override fun onFinish() {
            onEndGame()
        }
    }

}