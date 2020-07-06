package com.tsymbaliuk.domain.level.model

data class LevelModel(
    val index: Int,
    val name: String,
    val themeUri: String,
    val itemUris: List<String>?,
    var isFree: Boolean,
    var roundList: ArrayList<RoundModel>?
)

data class RoundModel(
    val index: Int,
    val numberOfItemsInSet: Int,
    val numberOfSets: Int,
    val time: Int
)