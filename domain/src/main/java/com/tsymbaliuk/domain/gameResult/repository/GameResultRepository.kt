package com.tsymbaliuk.domain.gameResult.repository

import com.tsymbaliuk.domain.gameResult.model.GameResultModel
import kotlinx.coroutines.flow.Flow

interface GameResultRepository {

    fun getAll(): Flow<List<GameResultModel>>

    fun saveGameResult(gameResult: GameResultModel)

}