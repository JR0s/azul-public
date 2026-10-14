package service.bot

import entity.AzulGameState
import entity.Color
import entity.Move

/**
 * This interface is used to enshure the Bot has certain funktionality and implements standart functions
 */
interface BotStrategy {
    //wrapper for 2 bots

    /**
     * Should hold the implementation of the bott which gets implementet
     */
    fun chooseMove(): Move

    /**
     * This funktion generates all Possible Moves for a given [AzulGameState]
     * @param gs The [AzulGameState] to calc on
     */
    fun getLegalMovesForCurrentPlayer(gs: AzulGameState): List<Move> {
        val currentPlayerIndex = gs.currentPlayer
        val currentPlayer = gs.players[currentPlayerIndex]
        val legalMoves = mutableListOf<Move>()

        /**
         * helper function: checks whether the given color already exists in the player's wall row
         */
        fun wallRowContainsColor(rowIndex: Int, color: Color): Boolean {
            val wallRow = currentPlayer.board.wall[rowIndex]
            return wallRow.any { it.filled && it.color == color }
        }

        /**
         * Helper function: add legal moves for a given source and its tiles
         */
        fun addMovesFromSource(sourceIndex: Int, tiles: List<Color>) {
            if (tiles.isEmpty()) return

            // iterate distinct colors, but exclude the start-marker token
            for (color in tiles.distinct().filter { it != Color.START_MARKER && it != Color.INVISIBLE }) {
                // pattern lines
                currentPlayer.board.patternLines.forEachIndexed { patLineIndex, patternLine ->
                    val hasSpace = patternLine.fill < patternLine.size
                    val matchesColorOrEmpty = (patternLine.color == Color.INVISIBLE || patternLine.color == color)

                    if (hasSpace && matchesColorOrEmpty) {
                        //Can't place to a pattern line if that color is already on the player's wall row
                        if (!wallRowContainsColor(patLineIndex, color)) {
                            legalMoves.add(Move(sourceIndex, color, patLineIndex))
                        }
                    }
                }

                // floor line index -1
                legalMoves.add(Move(sourceIndex, color, -1))
            }
        }

        // iterate factories (0..n-1)
        gs.factories.forEachIndexed { factIndex, factory ->
            addMovesFromSource(factIndex, factory.tiles)
        }

        // middle tiles index -1
        addMovesFromSource(-1, gs.middle.tiles)

        return legalMoves
    }
}