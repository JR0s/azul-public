package entity
import kotlinx.serialization.Serializable

@Serializable
/**
 * Class that represents middle area, from which tiles can be taken.
 *
 * @property tiles that are in the middle (their colors).
 */
class Middle(var tiles: MutableList<Color> = mutableListOf()){
    /**
     * Creates a deep copy of the Middle instance.
     * @return A new Middle instance with the same tiles.
     */
    fun copy(): Middle {
        return Middle(tiles.toMutableList())
    }
}