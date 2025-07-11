package com.example.ideapp

object LandingPageCardStore {
    val landingPageCards: MutableList<Card> = mutableListOf()
    fun removeCardById(cardId: String) {
        landingPageCards.removeAll { it.id == cardId }
    }
}
