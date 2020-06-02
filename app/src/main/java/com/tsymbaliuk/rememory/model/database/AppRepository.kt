package com.tsymbaliuk.rememory.model.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.tsymbaliuk.rememory.model.*

class AppRepository(private val appDao: AppDao,
                    private val firebaseManager: FirebaseManager) {

    val allGameResults: LiveData<List<GameResult>> = appDao.getAllGameResults()
    val allLevels: LiveData<ArrayList<Level>> = firebaseManager.getAllLevels()

    suspend fun insertGameResult(gameResult: GameResult) {
        appDao.insertGameResult(gameResult)
    }

}