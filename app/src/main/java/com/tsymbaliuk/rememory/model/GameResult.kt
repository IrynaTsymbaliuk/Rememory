package com.tsymbaliuk.rememory.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class GameResult (
    @PrimaryKey
    var gameResultId: String = "",
    val levelIndex: Int,
    val roundIndex: Int,
    var score: Long,
    var time: Long,
    var attempts: Int
)