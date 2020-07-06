package com.tsymbaliuk.domain.level.usecase

import com.tsymbaliuk.domain.level.model.LevelModel
import com.tsymbaliuk.domain.level.repository.LevelRepository
import kotlinx.coroutines.flow.Flow

class LevelUseCaseImpl(private val repository: LevelRepository):
    LevelUseCase {

    override fun getAll(): Flow<List<LevelModel>> = repository.getAll()

    override fun getItemUris(): List<String> {
        TODO("Not yet implemented")
    }

}