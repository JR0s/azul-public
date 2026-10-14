package gui.scenes.builder

import java.util.Vector
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Generates a list of positions arranged in a circle.
 *
 * @param objectCount The number of positions to generate.
 * @param radius The radius of the circle (default is 0.0).
 * @param posX The x-coordinate of the center of the circle (default is 0.0).
 * @param posY The y-coordinate of the center of the circle (default is 0.0).
 * @return A list of Pos objects representing the positions in the circle. And the rotation to the middel of the Circle
 */
fun circle(objectCount: Int, radius: Double = 0.0, posX: Double = 0.0, posY: Double = 0.0): List<Pos> {
    val iter = (0 until objectCount).map { it.toDouble() / objectCount }
    val mut: MutableList<Pos> = Vector(objectCount)
    iter.forEach { i ->
        val tx = radius * cos(i * 2 * Math.PI)
        val ty = radius * sin(i * 2 * Math.PI)
        val x = tx + posX
        val y = ty + posY
        val angle = (Math.toDegrees(atan2(posY - y, posX - x)) + 90) % 360
        mut.add(Pos(x, y, angle))
    }
    return mut.toList()
}


/**
 * Represents a position in a 2D space with x and y coordinates and an optional rotation.
 *
 * @property x The x-coordinate of the position.
 * @property y The y-coordinate of the position.
 * @property rotation The rotation angle in degrees (default is 0.0). To point to the middle of the board
 */
class Pos(val x: Double, val y: Double, val rotation: Double = 0.0)


