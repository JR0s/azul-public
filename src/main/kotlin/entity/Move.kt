package entity

/**
 * This class is Used to store a move for a Bot
 *
 * @property factory index of factory of which tiles are taken from, middle is represented as -1
 * @property color of tiles taken
 * @property patternLine index of patternLine in which tiles are placed, floorLine is represented as -1
 */
class Move(val factory: Int, val color: Color, val patternLine: Int)