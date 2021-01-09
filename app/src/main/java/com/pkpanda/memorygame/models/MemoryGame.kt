package com.pkpanda.memorygame.models

import com.pkpanda.memorygame.utiils.DEFAULT_ICONS

class MemoryGame(
    private val boardSize: BoardSize,
    private val customImages: List<String>?
) {

    val cards: List<MemoryCard>
    var numPairsFound = 0

    private var numCardsFlip = 0
    private var indexOfSingleFlippedCard: Int? = null

    init {
        if (customImages == null) {
            var chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
            var randomizedImages = (chosenImages + chosenImages).shuffled()
            cards = randomizedImages.map { MemoryCard(it) }
        } else {
            val randomizedImages = (customImages + customImages).shuffled()
            cards = randomizedImages.map { MemoryCard(it.hashCode(), it) }
        }

    }

    fun flipCard(position: Int): Boolean {
        numCardsFlip++
        var foundMatch = false
        val card = cards[position]
        if (indexOfSingleFlippedCard == null) {
            restoreCards()
            indexOfSingleFlippedCard = position
        } else {
            foundMatch = checkforMatch(indexOfSingleFlippedCard!!, position)
            indexOfSingleFlippedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch

    }

    private fun checkforMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].identifier != cards[position2].identifier) {
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }


    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp

    }

    fun getNumMoves(): Int {
        return (numCardsFlip / 2)
    }
}