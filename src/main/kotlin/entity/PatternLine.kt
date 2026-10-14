package entity
import kotlinx.serialization.Serializable

@Serializable
/**
 * Class that represents pattern lines on players` boards.
 *
 * @property size how many tiles this pattern line has.
 * @property fill how many tiles are already filled.
 * @property color color of tiles that were already placed on this pattern line
 */
class PatternLine(val size: Int, var fill: Int = 0, var color: Color = Color.INVISIBLE){
    /**
     * Creates a deep copy of the PatternLine instance.
     * @return A new PatternLine instance with the same size, fill, and color.
     */
    fun copy(): PatternLine {
        return PatternLine(size, fill, color)
    }
}