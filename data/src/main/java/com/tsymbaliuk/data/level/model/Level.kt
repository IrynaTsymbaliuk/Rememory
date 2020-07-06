package com.tsymbaliuk.data.level.model

data class Level(
    val indexNumber: Int? = null,
    val levelName: String? = null,
    val themeDrawable: String? = null,
    val itemDrawables: ArrayList<String>? = null,
    var isFree: Boolean? = null,
    var rounds: ArrayList<Round>? = null
)



