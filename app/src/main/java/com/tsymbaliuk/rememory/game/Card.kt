package com.tsymbaliuk.rememory.game

enum class CardState() {
        OPEN, CLOSE, DISCLOSE
}

class Card(
        var drawableUri: String,
        var state: CardState
)