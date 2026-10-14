package service.bot

import entity.*


/**
 * Holds data and Funktionaty of min/Max bot
 * @property state holds the current [AzulGameState]
 * @property depth holds the deepth at witch the algorythym should run
 */
class MinMaxBot(
    private val state: AzulGameState,
    private val depth: Int = 2
) : BotStrategy {

    /**
     * Chooses the best move for the current player by using the [minmax] algorithm.
     * Falls back to the first legal move if none is found.
     */
    override fun chooseMove(): Move {
        val (bestMove, _) = minmax(state, depth, true)
        return bestMove ?: getLegalMovesForCurrentPlayer(state).firstOrNull()
        ?: throw IllegalStateException("No legal moves available")
    }

    /**
     * Recursive MinMax algorithm.
     *
     * @param state The game state to evaluate from.
     * @param depth Remaining depth to search.
     * @param maximizingPlayer Whether the current player is maximizing or minimizing.
     * @return A pair of (best move, score).
     */

    private fun minmax(
        state: AzulGameState,
        depth: Int,
        maximizingPlayer: Boolean
    ): Pair<Move?, Int> {

        val currentPlayerIdx = state.currentPlayer

        // Base case: reached max depth
        if (depth == 0 ) { return null to evaluateStateBot(state, currentPlayerIdx) }

        var bestMove: Move? = null

        return if (maximizingPlayer) {

            var bestValue = Int.MIN_VALUE


            // Explore all possible moves
            for (move in MinMaxBot(state).getLegalMovesForCurrentPlayer(state)) {
                val next = applyMove(state, move)        // simulate move
                val (_, value) = minmax(next, depth - 1, false) // evaluate opponent's turn
                if (value > bestValue) {                 // keep best max score
                    bestValue = value
                    bestMove = move
                }
            }
            bestMove to bestValue


        } else {

            var bestValue = Int.MAX_VALUE


            // Explore all possible moves
            for (move in MinMaxBot(state).getLegalMovesForCurrentPlayer(state)) {
                val next = applyMove(state, move)        // simulate move
                val (_, value) = minmax(next, depth - 1, true) // evaluate player's turn
                if (value < bestValue) {                 // keep the best min score
                    bestValue = value
                    bestMove = move
                }
            }
            bestMove to bestValue
        }
    }

    /**
     * evaluate the current state of the game
     * points after the turn +
     * percent of row filled (1 tile / patternLine 1) (2tiles/3space)
     */
     private fun evaluateStateBot(game: AzulGameState, currentPlayer : Int): Int{
        // score plus points for the player
        val boardPoints = this.scoreBoardBot(game, currentPlayer)

        // Calculates fraction of bonus points
        val bonusPoints = this.scoreBonusBot(game, currentPlayer)

        val newScore = boardPoints + bonusPoints

        return newScore
    }

    /**
     * function to apply a given move to a given game state
     * @property state the current state the move is applied to
     * @property move the applied move
     */
    private fun applyMove(state: AzulGameState, move: Move): AzulGameState {
    val copy = deepCopy(state)
    val player = copy.players[copy.currentPlayer]
    val takenTiles: MutableList<Color> = mutableListOf()
    if (move.factory == -1) {
        require(move.color != Color.START_MARKER) { "Cannot pick START_MARKER directly" }
        takenTiles.addAll(copy.middle.tiles.filter { it == move.color })
        copy.middle.tiles.removeAll { it == move.color }

        if (Color.START_MARKER in state.middle.tiles) {
            copy.middle.tiles.remove(Color.START_MARKER)
            val floor = player.board.floorLine
            val firstEmpty = player.board.floorLine.indexOfFirst { it == Color.INVISIBLE }
            if(firstEmpty < 7) floor[firstEmpty] = Color.START_MARKER
        }
    } else {
        require(move.color != Color.START_MARKER) { "START_MARKER cannot be in factories" }

        val factory = copy.factories[move.factory]
        takenTiles.addAll(factory.tiles.filter { it == move.color })

        // Rest in die Mitte
        factory.tiles.forEach { if (it != move.color) copy.middle.tiles.add(it) }
        factory.tiles.clear()
    }

    if (move.patternLine == -1) {
        // Alles auf FloorLine
        for (tile in takenTiles) {
            this.moveToFloorLine(copy, tile)
        }
    } else {
        val line = player.board.patternLines[move.patternLine]

        // falls Linie noch leer → Farbe setzen
        if (line.color == Color.INVISIBLE) {
            line.color = move.color
        }
        check(line.color == move.color) { "PatternLine already has a different color" }

        val space = line.size - line.fill
        val toPlace = takenTiles.take(space)
        val toFloor = takenTiles.drop(space)

        line.fill += toPlace.size

        // Überschüssige Tiles auf die FloorLine
        for (tile in toFloor) {
            this.moveToFloorLine(copy, tile)
        }
    }
    copy.currentPlayer = (copy.currentPlayer + 1) % copy.players.size
    return copy
}
    /**
     * function to move tiles to the floorLine
     */
    private fun moveToFloorLine(state : AzulGameState, tile: Color){
        check(tile != Color.START_MARKER){"Start marker is not allowed here"}
        val player =  state.players[state.currentPlayer]
        val floorLineIdx = player.board.floorLine.indexOfFirst { it == Color.INVISIBLE }
        if(floorLineIdx > -1){
            player.board.floorLine[floorLineIdx] = tile
        }
        else{
            state.usedTiles.add(tile)
        }
    }

    /**
     * function to deepCopy a game state
     */
    private fun deepCopy(state: AzulGameState): AzulGameState {
        val copiedPlayers = state.players.map { player ->
            Player(
                name = player.name,
                score = player.score,
                playerType = player.playerType,
                board = Board(
                    patternLines = player.board.patternLines.map { line ->
                        PatternLine(
                            size = line.size,
                            fill = line.fill,
                            color = line.color
                        )
                    }.toTypedArray(),
                    floorLine = player.board.floorLine.toMutableList()
                )
            )
        }.toMutableList()

        val copy = AzulGameState(
            currentPlayer = state.currentPlayer,
            state = state.state,
            players = copiedPlayers
        )

        copy.middle = Middle(state.middle.tiles.toMutableList())
        copy.factories = state.factories.map { f -> Factory(f.tiles.toMutableList()) }.toMutableList()
        copy.bag = state.bag.toMutableList()
        copy.usedTiles = state.usedTiles.toMutableList()
        copy.takenTiles = state.takenTiles.copy()

        return copy
    }

    /**
     * calculates the points from board
     */
    private fun scoreBoardBot(game: AzulGameState, currentPlayer : Int): Int{
        val player = game.players[currentPlayer]
        // Calculates score after each round
        var newScore = 0
        // score plus points for the player
        for(patternLine in player.board.patternLines){
            if(patternLine.size == patternLine.fill){
                val lineColor = patternLine.color
                val lineIndex = player.board.patternLines.indexOf(patternLine)

                newScore += this.getScoreBot(yPos = lineIndex, tileColor = lineColor, player = player)
            }
        }

        //score minus points of the player
        when(player.board.floorLine.size){
            1 -> newScore -= 1
            2 -> newScore -= 2
            3 -> newScore -= 4
            4 -> newScore -= 6
            5 -> newScore -= 8
            6 -> newScore -= 11
            7 -> newScore -= 14
            else -> Unit
        }
        return newScore
    }

    /* ALTERNATIVE METHOD FOR SCORING THE POINTS ON THE BOARD:
      Give points for a fraction of a filled patternLine!

    /**
     * calculates the points from board
     */
    private fun scoreBoardBot(game: AzulGameState, currentPlayer : Int): Int{
        val player = game.players[currentPlayer]
        // Calculates score after each round
        var newScore = 0
        // score plus points for the player
        for(patternLine in player.board.patternLines){
            val lineColor = patternLine.color
            checkNotNull(lineColor){"The line has no color"}
            val lineIndex = player.board.patternLines.indexOf(patternLine)

            newScore = newScore +
            this.getScoreBot(yPos = lineIndex, tileColor = lineColor,
             player = player)*(patternLine.size/patternLine.fill)
        }

        //score minus points of the player
        when(player.board.floorLine.size){
            1 -> newScore = newScore-1
            2 -> newScore = newScore-2
            3 -> newScore = newScore-4
            4 -> newScore = newScore-6
            5 -> newScore = newScore-8
            6 -> newScore = newScore-11
            7 -> newScore = newScore-14
            else -> Unit
        }
        return newScore
    }
     */

    /**
     * calculates the bonus points
     */
    private fun scoreBonusBot(game: AzulGameState, currentPlayer : Int): Int{
        val player = game.players[currentPlayer]
        var newScore = 0

        // full rows
        for(line in player.board.wall){
            val fractionFilled = line.count{it.filled}
            newScore += 2*(fractionFilled/line.size)
        }

        // full columns
        for(i in 0..4){
            val ithRow : List<WallTile> = player.board.wall.map{ row -> row[i] }
            val fractionFilled = ithRow.count{ it.filled}
            newScore += 7*(fractionFilled/ithRow.size)
        }

        // full colors
        for(color in arrayOf(Color.RED, Color.BLUE, Color.YELLOW, Color.WHITE, Color.BLACK)){
            val wallColor : List<WallTile> = player.board.wall.flatten().filter{it.color == color}
            val fractionFilled = wallColor.count{ it.filled}
            newScore += 10*(fractionFilled/5)
        }
        return newScore
    }

    /**
     *  Calculates the score for a given tile for each row and column
     *  this method only scores the points for adjacent tiles
     */
    private fun getScoreBot(yPos: Int, tileColor: Color, player:Player):Int{
        var xPoints = 0
        var yPoints = 0
        val xPos = player.board.wall[yPos].indexOfFirst{it.color == tileColor}

        //horizontal points
        var xPointer = xPos-1
        while(xPointer>=0 && player.board.wall[yPos][xPointer].filled){
            xPoints++
            xPointer--
        }
        xPointer = xPos+1
        while(xPointer < 5 && player.board.wall[yPos][xPointer].filled){
            xPoints++
            xPointer++
        }

        if(xPoints > 0)xPoints++ // points for the tile itself

        //vertical points
        var yPointer = yPos-1
        while(yPointer >=0 && player.board.wall[yPointer][xPos].filled){
            yPoints++
            yPointer--
        }
        yPointer = yPos+1
        while(yPointer < 5 && player.board.wall[yPointer][xPos].filled){
            yPoints++
            yPointer++
        }

        if(yPoints > 0) yPoints++ // points for the tile itself

        if(xPoints == 0 && yPoints == 0) return 1
        return xPoints + yPoints
    }}



