package entity
import kotlinx.serialization.Serializable

@Serializable
/**
 * Class that represents players in game.
 *
 * @property board is the Board object with patternLines, floor and wall.
 * @property playerType enum that shows if it is player, online opponent or bot.
 * @property name Name of this player/Bot.
 * @property score Current score of this player (shown on top of the board).
 */
class Player(val name: String, var score: Int,
             val board: Board, val playerType: PlayerType){


    /**
     * Implemts a deepCopy of the [entity.Player]
     */
    fun copy(): Player {
        return Player(name, score, board.copy(), playerType)
    }
}