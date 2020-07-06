package com.tsymbaliuk.domain.level.usecase

import com.tsymbaliuk.domain.level.model.LevelModel
import kotlinx.coroutines.flow.Flow

interface LevelUseCase {

    fun getAll(): Flow<List<LevelModel>>

    fun getItemUris(): List<String>

}