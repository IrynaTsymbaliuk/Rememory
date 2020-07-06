package com.tsymbaliuk.domain.gameResult.model

data class GameResultModel (
    val gameResultId: String,
    val levelIndex: Int,
    val roundIndex: Int,
    var score: Long,
    var time: Long,
    var attempts: Int
)