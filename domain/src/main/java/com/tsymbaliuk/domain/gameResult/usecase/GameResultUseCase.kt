package com.tsymbaliuk.domain.gameResult.usecase

import com.tsymbaliuk.domain.gameResult.model.GameResultModel
import kotlinx.coroutines.flow.Flow

interface GameResultUseCase {

    fun getAll(): Flow<List<GameResultModel>>

    fun getNext(): GameResultModel

    fun getLast(): GameResultModel

    fun saveGameResult(gameResult: GameResultModel)

}