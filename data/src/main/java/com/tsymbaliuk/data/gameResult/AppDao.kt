package com.tsymbaliuk.data.gameResult

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tsymbaliuk.data.gameResult.model.GameResult
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM GameResult")
    fun getAllGameResults(): Flow<List<GameResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGameResult(gameResult: GameResult)

}