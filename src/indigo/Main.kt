package indigo2

import kotlin.collections.ArrayDeque
import kotlin.random.Random

enum class TURN(private val action: String) {
    PLAYER("player"), COMPUTER("computer"), EXIT("exit"), UNKNOWN("unknown");

    fun getEnum(action: String): TURN {
        for (value in TURN.values()) {
            if (value.action == action) {
                return value
            }
        }
        return UNKNOWN
    }
}

var cardsOnTable = mutableListOf<String>()

var playerTakenCards = ArrayDeque<String>()
var computerTakenCards = ArrayDeque<String>()
var firstTurn: TURN = TURN.UNKNOWN
var lastTakenBy: TURN = TURN.UNKNOWN

fun main() {
    var cards = prepareCards()
    var deck = mutableListOf<String>()
    var numOfCards = 0;
    var selectedCards = mutableListOf<String>()
    var num = ""
    var reseted = false
    var opt: String
    var cardPlay: String
    var turn: TURN
    var cardsOnPlayer = ArrayDeque<String>()
    var cardsOnComputer = ArrayDeque<String>()

    println("Indigo Card Game")
    do {
        println("Play first?")
        opt = readln()
        if (opt.lowercase() == "yes") {
            turn = TURN.PLAYER
        } else if (opt.lowercase() == "no") {
            turn = TURN.COMPUTER
        } else if (opt.lowercase() == "exit") {
            turn = TURN.EXIT
        } else {
            turn = TURN.UNKNOWN
        }
    } while (turn == TURN.UNKNOWN)

    firstTurn = turn
    //Deck is initialized
    deck.addAll(cards)
    deck = deck.shuffled().toMutableList()

    for (i in 1..4) {
        cardsOnTable.add(deck.first())
        deck.removeFirst()
    }

    println("Initial cards on the table: ")
    println(cardsOnTable.joinToString(" "))
    println()

    while (turn != TURN.EXIT) {

        if (cardsOnTable.isNotEmpty()) {
            println("${cardsOnTable.size} cards on the table, and the top card is ${cardsOnTable.last()}")
        } else {
            println("No cards on the table")
        }

        if (playerTakenCards.size + computerTakenCards.size + cardsOnTable.size == 52) {
            printStatus(true)
            turn = TURN.EXIT
            continue
        }



        if (deck.isNotEmpty()) {
            if (cardsOnPlayer.isEmpty()) {
                for (i in 1..6) {
                    cardsOnPlayer.add(deck.first())
                    deck.removeFirst()
                }
            }

            if (cardsOnComputer.isEmpty()) {
                for (i in 1..6) {
                    cardsOnComputer.add(deck.first())
                    deck.removeFirst()
                }
            }
        }
        var cInd: Int

        if (turn == TURN.PLAYER) {
            val str = StringBuilder("Cards in hand:")

            for (i in 0 until cardsOnPlayer.size) {
                str.append(" ${i + 1})${cardsOnPlayer[i]}")
            }
            println(str.toString())
            do {
                println("Choose a card to play (1-${cardsOnPlayer.size}):")
                try {
                    cardPlay = readln()
                    if (cardPlay.lowercase() == "exit") {
                        turn = TURN.EXIT
                        cInd = -1
                        continue
                    }
                    cInd = cardPlay.toInt()
                    if (cInd !in 1..cardsOnPlayer.size) {
                        cInd = 0
                    } else {
                        if (cardsOnTable.isNotEmpty() && isWonCards(
                                cardsOnTable[cardsOnTable.size - 1],
                                cardsOnPlayer[cInd - 1]
                            )
                        ) {
                            playerTakenCards.addAll(cardsOnTable)
                            playerTakenCards.add(cardsOnPlayer[cInd - 1])
                            cardsOnPlayer.removeAt(cInd - 1)
                            cardsOnTable.clear()
                            lastTakenBy = TURN.PLAYER
                            //if (playerTakenCards.size + computerTakenCards.size + cardsOnTable.size != 52) {
                            println("Player wins cards")
                            //}
                            //if (playerTakenCards.size + computerTakenCards.size + cardsOnTable.size != 52) {
                            printStatus(false)
                            //}
                        } else {
                            cardsOnTable.add(cardsOnPlayer[cInd - 1])
                            cardsOnPlayer.removeAt(cInd - 1)
                        }

                    }
                } catch (e: Exception) {
                    cInd = 0
                }
            } while (cInd == 0)

        } else if (turn == TURN.COMPUTER) {
            val str = StringBuilder()
            for (i in 0 until cardsOnComputer.size) {
                str.append("${cardsOnComputer[i]} ")
            }

            println(str.removeSuffix(" "))
            print("Computer plays ")
            val compCard = playCompCard(cardsOnComputer)
            //cInd = Random.nextInt(1, cardsOnComputer.size + 1)

            println(compCard)
            if (cardsOnTable.isNotEmpty() && isWonCards(
                    cardsOnTable[cardsOnTable.size - 1],
                    compCard
                )
            ) {
                computerTakenCards.addAll(cardsOnTable)
                computerTakenCards.add(compCard)
                //cardsOnComputer.removeAt(cInd - 1)
                cardsOnTable.clear()
                lastTakenBy = TURN.COMPUTER
                //if (playerTakenCards.size + computerTakenCards.size + cardsOnTable.size != 52) {
                println("Computer wins cards")
                //}
                //if (playerTakenCards.size + computerTakenCards.size + cardsOnTable.size != 52) {
                printStatus(false)
                //}
            } else {
                cardsOnTable.add(compCard)
                //cardsOnComputer.removeAt(cInd - 1)
            }

        }

        if (turn == TURN.PLAYER) {
            turn = TURN.COMPUTER
        } else if (turn == TURN.COMPUTER) {
            turn = TURN.PLAYER
        }

    }

    println("Game Over")

}

fun playCompCard(cards: ArrayDeque<String>): String {
    val mapBySuit = mutableMapOf<Char, MutableList<String>>()
    val mapByRank = mutableMapOf<String, MutableList<String>>()
    var candidates = mutableListOf<String>()
    var retCandidate: String

    if (cards.size == 1) {
        return cards.removeFirst()
    } else {
        if (cardsOnTable.isNotEmpty()) {
            for (c in cards) {
                if (c.first() == cardsOnTable.last().first() || c.last() == cardsOnTable.last().last()) {
                    candidates.add(c)
                }
            }
            if (candidates.isNotEmpty()) {
                if (candidates.size == 1) {
                    cards.remove(candidates.first())
                    return candidates.first()
                } else {
                    return getAnySuitableCard(cards, candidates, mapBySuit, mapByRank)
                }

            } else {
                return getSuitableCard(cards, mapBySuit, mapByRank)
            }

        } else {
            return getSuitableCard(cards, mapBySuit, mapByRank)
        }
    }
}

fun getSuitableCard(
    cards: ArrayDeque<String>,
    mapBySuit: MutableMap<Char, MutableList<String>>,
    mapByRank: MutableMap<String, MutableList<String>>
): String {

    var candidates = mutableListOf<String>()
    var retCandidate: String

    for (card in cards) {
        mapBySuit.putIfAbsent(card.last(), mutableListOf<String>())
        mapByRank.putIfAbsent(card.substring(0, card.length - 1), mutableListOf<String>())
        mapBySuit[card.last()]!!.add(card)
        mapByRank[card.substring(0, card.length - 1)]!!.add(card)
    }
    // If there are cards in hand with the same suit, throw one of them at random
    for (entry in mapBySuit) {
        if (entry.value.size > 1) {
            candidates.addAll(entry.value)
        }
    }
    if (candidates.isNotEmpty()) {
        retCandidate = candidates[Random.nextInt(candidates.size)]
        cards.remove(retCandidate)
        return retCandidate
    }
    /*If there are no cards in hand with the same suit, but there are cards with the same rank,
      then throw one of them at random
    */
    for (entry in mapByRank) {
        if (entry.value.size > 1) {
            candidates.addAll(entry.value)
        }
    }
    if (candidates.isNotEmpty()) {
        retCandidate = candidates[Random.nextInt(candidates.size)]
        cards.remove(retCandidate)
        return retCandidate
    }
    //If there are no cards in hand with the same suit or rank, throw any card at random
    retCandidate = cards[Random.nextInt(cards.size)]
    cards.remove(retCandidate)
    return retCandidate
}

fun getAnySuitableCard(
    cards: ArrayDeque<String>,
    parCandidates: MutableList<String>,
    mapBySuit: MutableMap<Char, MutableList<String>>,
    mapByRank: MutableMap<String, MutableList<String>>
): String {

    var candidates = mutableListOf<String>()
    var retCandidate: String

    for (card in parCandidates) {

        mapBySuit.putIfAbsent(card.last(), mutableListOf<String>())
        mapByRank.putIfAbsent(card.substring(0, card.length - 1), mutableListOf<String>())
        mapBySuit[card.last()]!!.add(card)
        mapByRank[card.substring(0, card.length - 1)]!!.add(card)


    }
    // If there are cards in hand with the same suit, throw one of them at random
    for (entry in mapBySuit) {
        if (entry.value.size > 1) {
            candidates.addAll(entry.value)
        }
    }
    if (candidates.isNotEmpty()) {
        retCandidate = candidates[Random.nextInt(candidates.size)]
        cards.remove(retCandidate)
        return retCandidate
    }
    /*If there are no cards in hand with the same suit, but there are cards with the same rank,
      then throw one of them at random
    */
    for (entry in mapByRank) {
        if (entry.value.size > 1) {
            candidates.addAll(entry.value)
        }
    }
    if (candidates.isNotEmpty()) {
        retCandidate = candidates[Random.nextInt(candidates.size)]
        cards.remove(retCandidate)
        return retCandidate
    }
    //If there are no cards in hand with the same suit or rank, throw any card at random
    retCandidate = parCandidates[Random.nextInt(parCandidates.size)]
    cards.remove(retCandidate)
    return retCandidate
}

fun isWonCards(card1: String, card2: String): Boolean {
    return card1.first() == card2.first() || card1.last() == card2.last()
}

fun printStatus(finish: Boolean) {
    var playerScore = 0
    var computerScore = 0
    if (finish && playerTakenCards.size + computerTakenCards.size + cardsOnTable.size == 52) {
        if (lastTakenBy == TURN.PLAYER) {
            playerTakenCards.addAll(cardsOnTable)
            cardsOnTable.clear()
        } else if (lastTakenBy == TURN.COMPUTER) {
            computerTakenCards.addAll(cardsOnTable)
            cardsOnTable.clear()
        } else { // no one took cards
            if (firstTurn == TURN.PLAYER) {
                playerTakenCards.addAll(cardsOnTable)
                cardsOnTable.clear()
            } else {
                computerTakenCards.addAll(cardsOnTable)
                cardsOnTable.clear()
            }
        }
        if (playerTakenCards.size > computerTakenCards.size) {
            playerScore += 3
        } else if (computerTakenCards.size > playerTakenCards.size) {
            computerScore += 3
        } else {
            if (firstTurn == TURN.PLAYER) {
                playerScore += 3
            } else if (firstTurn == TURN.COMPUTER) {
                computerScore += 3
            }
        }
    }

    for (playerCard in playerTakenCards) {
        if (listOf('A', 'K', 'Q', 'J', '1').contains(playerCard.first())) {
            playerScore++
        }
    }
    for (computerCard in computerTakenCards) {
        if (listOf('A', 'K', 'Q', 'J', '1').contains(computerCard.first())) {
            computerScore++
        }
    }

    println("Score: Player $playerScore - Computer $computerScore")
    println("Cards: Player ${playerTakenCards.size} - Computer ${computerTakenCards.size}")
    println()
}

fun prepareCards(): MutableList<String> {
    val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    val suits = listOf('\u2666', '\u2665', '\u2660', '\u2663')
    val cards = mutableListOf<String>()

    for (i in ranks.indices) {
        for (j in suits.indices) {
            cards.add(ranks[i] + suits[j])
        }
    }

    return cards
}