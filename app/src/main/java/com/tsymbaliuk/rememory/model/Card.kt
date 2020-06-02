package com.tsymbaliuk.rememory.model

enum class CardState() {
        OPEN, CLOSE, DISCLOSE
}

class Card(
        var drawableUri: String,
        var state: CardState
)