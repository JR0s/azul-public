package service.bot

import entity.AzulGameState
import entity.Move
import kotlin.random.Random

/**
 * Holds the funktionality of the random Bot
 * @property state holds the current [AzulGameState]
 */
class RandomBot(private val state: AzulGameState): BotStrategy {
    private val rnd = Random.Default

    /**
     * implements the chooseMove function for the random bot
     */
    override fun chooseMove(): Move {
        // get all legal moves for this bot
        val moves = getLegalMovesForCurrentPlayer(state)

        // pick one randomly
        return moves[rnd.nextInt(moves.size)]
    }
}