package com.tsymbaliuk.domain.level.repository

import com.tsymbaliuk.domain.level.model.LevelModel
import kotlinx.coroutines.flow.Flow

interface LevelRepository {

    fun getAll(): Flow<List<LevelModel>>

}