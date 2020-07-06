package com.tsymbaliuk.data.gameResult

import androidx.lifecycle.MutableLiveData
import com.tsymbaliuk.data.toGameResult
import com.tsymbaliuk.domain.gameResult.model.GameResultModel
import com.tsymbaliuk.domain.gameResult.repository.GameResultRepository
import com.tsymbaliuk.data.toGameResultModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GameResultRepositoryImpl(private val appDao: AppDao) : GameResultRepository {

    override fun getAll(): Flow<List<GameResultModel>> {
       return flow {
           appDao.getAllGameResults()
       }
    }

    override fun saveGameResult(gameResult: GameResultModel) {
        appDao.insertGameResult(gameResult.toGameResult())
    }

}