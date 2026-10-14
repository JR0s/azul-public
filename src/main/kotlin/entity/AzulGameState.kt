package entity
import kotlinx.serialization.Serializable

/**
 * Main class representing the full state of an Azul game.
 *
 * @property currentPlayer The index of the current active player in [players].
 * @property state The current [GameState] of the game.
 * @property players Mutable list of players participating in the game.
 * @property middle The center area from which players can take tiles.
 * @property factories Mutable list of factories containing tiles available to players.
 * @property bag Mutable list of all tiles that have not been played yet.
 * @property usedTiles Mutable list of tiles that have been played and removed from active play.
 * @property takenTiles Pair containing the color and amount of tiles taken by the current player.
 */
@Serializable
class AzulGameState(
    var currentPlayer: Int,
    var state: GameState,
    var players: MutableList<Player>,
    var online : Boolean = false
) {
    var middle: Middle = Middle()
    var factories: MutableList<Factory> = mutableListOf()
    var bag: MutableList<Color> = mutableListOf()
    var usedTiles: MutableList<Color> = mutableListOf()
    var takenTiles: Pair<Color, Int> = Color.INVISIBLE to 0

    /**
     * Implements a deep Copy of [entity.AzulGameState]
      */
    fun copy() : AzulGameState {
        val az =AzulGameState(currentPlayer,state,
            (players.map { it.copy() }).toMutableList())
        // deepCopy
        az.middle=middle.copy()
        az.factories= factories.map { it.copy() }.toMutableList()
        az.bag=bag.toMutableList()
        az.usedTiles=usedTiles.toMutableList()
        az.takenTiles=takenTiles.copy()
        return az
    }
}
