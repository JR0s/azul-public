package entity
import kotlinx.serialization.Serializable

@Serializable
/**
 * Class that represents factories, from which tiles can be taken.
 *
 * @property tiles that are in the factory (their colors).
 */
class Factory(var tiles: MutableList<Color> = mutableListOf()){
    /**
     * Creates a deep copy of the Factory instance.
     * @return A new Factory instance with the same tiles.
     */
    fun copy(): Factory {
        return Factory(tiles.map { it }.toMutableList())
    }
}