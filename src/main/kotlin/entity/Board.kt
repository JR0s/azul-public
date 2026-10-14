package entity
import kotlinx.serialization.Serializable

@Serializable
/**
 * Class that represents players` boards.
 *
 * @property patternLines list of 5 PatternLine objects to be filled with tiles.
 * @property wall 2D array of WallTiles.
 * @property floorLine the line to be filled with rest tiles and counted as minus points.
 */
data class Board(var patternLines: Array<PatternLine>,
                 var floorLine: MutableList<Color> = MutableList(7){ Color.INVISIBLE }) {
                 var  wall: Array<Array<WallTile>> = arrayOf(
        arrayOf(
            WallTile(false, Color.BLUE),
            WallTile(false, Color.YELLOW),
            WallTile(false, Color.RED),
            WallTile(false, Color.BLACK),
            WallTile(false, Color.WHITE)),

        arrayOf(
            WallTile(false, Color.WHITE),
            WallTile(false, Color.BLUE),
            WallTile(false, Color.YELLOW),
            WallTile(false, Color.RED),
            WallTile(false, Color.BLACK)),

        arrayOf(
            WallTile(false, Color.BLACK),
            WallTile(false, Color.WHITE),
            WallTile(false, Color.BLUE),
            WallTile(false, Color.YELLOW),
            WallTile(false, Color.RED)),

        arrayOf(
            WallTile(false, Color.RED),
            WallTile(false, Color.BLACK),
            WallTile(false, Color.WHITE),
            WallTile(false, Color.BLUE),
            WallTile(false, Color.YELLOW)),

        arrayOf(
            WallTile(false, Color.YELLOW),
            WallTile(false, Color.RED),
            WallTile(false, Color.BLACK),
            WallTile(false, Color.WHITE),
            WallTile(false, Color.BLUE)),
        )

    /**
     * Generates a deepCopy of the [entity.Board] Objekt
     */
    fun copy(): Board {
        val newPatternLines = patternLines.map { it.copy() }.toTypedArray()
        val newFloorLine = floorLine.toMutableList()
        return Board(newPatternLines, newFloorLine).apply {
            wall = wall.map { it.copyOf() }.toTypedArray()
        }
    }
}