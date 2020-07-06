package com.tsymbaliuk.data


import com.tsymbaliuk.domain.gameResult.model.GameResultModel
import com.tsymbaliuk.data.gameResult.model.GameResult
import com.tsymbaliuk.data.level.model.Level
import com.tsymbaliuk.data.level.model.Round
import com.tsymbaliuk.domain.level.model.LevelModel
import com.tsymbaliuk.domain.level.model.RoundModel

fun GameResult.toGameResultModel() =
    GameResultModel(
        gameResultId,
        levelIndex,
        roundIndex,
        score,
        time,
        attempts
    )

fun GameResultModel.toGameResult() =
    GameResult(
        gameResultId,
        levelIndex,
        roundIndex,
        score,
        time,
        attempts
    )

fun Level.toLevelModel() =
    LevelModel(
        indexNumber ?: 0,
        levelName ?: "",
        themeDrawable ?: "",
        itemDrawables,
        isFree ?: true,
        if (!rounds.isNullOrEmpty())
            rounds?.map { t -> t.toRoundModel() } as ArrayList<RoundModel> else arrayListOf())

fun Round.toRoundModel() =
    RoundModel(
        indexNumber ?: 0,
        numberOfItemsInSet ?: 0,
        numberOfSets ?: 0,
        time ?: 0
    )