package com.tsymbaliuk.rememory.model.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tsymbaliuk.rememory.model.*

@Dao
interface AppDao {

    @Query("SELECT * FROM GameResult")
    fun getAllGameResults(): LiveData<List<GameResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameResult(game: GameResult)

}