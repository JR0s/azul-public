package entity
import kotlinx.serialization.Serializable

@Serializable
/**
 * Class that represents tiles in wall of players` boards.
 *
 * @property filled if this tile is filled (when the pattern line was filled)
 * @property color the color of this tile
 */
class WallTile(var filled: Boolean = false, var color: Color){
    /**
     * Creates a deep copy of the WallTile instance.
     * @return A new WallTile instance with the same filled status and color.
     */
    fun copy(): WallTile {
        return WallTile(filled, color)
    }
}