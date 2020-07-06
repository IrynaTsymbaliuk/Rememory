package com.tsymbaliuk.domain.gameResult.usecase

import com.tsymbaliuk.domain.gameResult.model.GameResultModel
import com.tsymbaliuk.domain.gameResult.repository.GameResultRepository
import kotlinx.coroutines.flow.Flow

class GameResultUseCaseImpl(private val repository: GameResultRepository):
    GameResultUseCase {

    override fun getAll(): Flow<List<GameResultModel>> = repository.getAll()

    override fun getNext(): GameResultModel {
        TODO("Not yet implemented")
    }

    override fun getLast(): GameResultModel {
        TODO("Not yet implemented")
    }

    override fun saveGameResult(gameResult: GameResultModel) = repository.saveGameResult(gameResult)
}