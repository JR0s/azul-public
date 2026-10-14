package service
import entity.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


/**
 * Service layer class that provides the logic for the possible actions of a player
 *
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Nimmt alle Spielsteine der angegebenen [color] aus der gewählten [Factory]
     * und verschiebt alle übrigen Steine aus dieser Fabrik in die gemeinsame Mitte.
     *
     * @param factoryIndex Die Fabrik, aus der die Steine genommen werden.
     * @param color Die Farbe der Steine, die entnommen werden sollen.
     *
     * Vorbedingungen:
     * - Es muss der Zug des aktuellen Spielers sein (`currentPlayer` in Game).
     * - Die angegebene [Factory] darf nicht leer sein.
     * - Der Spielzustand muss auf "TAKE" gesetzt sein.
     *
     * Nachbedingungen:
     * - Alle Steine der gewählten [color] werden aus [Factory.tiles] entfernt.
     * - Diese Steine werden im temporären Speicher (`takenTiles`) des Spiels gespeichert.
     * - Alle übrigen Steine aus [Factory.tiles] werden in `middle.tiles` verschoben.
     * - Die [Factory.tiles] ist danach leer.
     * - Der Spielzustand wird auf "PLACE" gesetzt.
     *
     * @returns Diese Methode hat keinen Rückgabewert (`Unit`).
     *
     * @throws IllegalStateException, wenn es nicht der Zug des Spielers ist.
     * @throws IllegalArgumentException, wenn [Factory] leer ist oder keinen Stein der [color] enthält.
     *
     * @sample takeTilesFromFactory(factories[2], Color.BLUE)
     */
    fun takeTilesFromFactory(factoryIndex: Int, color: Color) {
        println("FROMFACTORYYYYYYYY")
        val game = rootService.currentGame ?: error("No active game")
        val gs = game.azulGameState
        val factory = gs.factories[factoryIndex]
        game.redoStack.clear()

        require(gs.currentPlayer in gs.players.indices) { "It's not currently any player's turn" }
        check(gs.state == GameState.TAKE) { "Game state ${gs.state} must be TAKE to take tiles from factory" }
        require(factory.tiles.isNotEmpty()) { "The selected factory is empty" }
        require(factory.tiles.any { it == color }) {
            "The selected factory doesn't contain tiles of the specified color"
        }

        val takenCount = factory.tiles.count { it == color }
        val remaining  = factory.tiles.filter { it != color }
        // record taken tiles as (color, amount)
        gs.takenTiles = color to takenCount

        // move leftovers to middle
        gs.middle.tiles = (gs.middle.tiles + remaining) as MutableList<Color>
        // empty factory by reassignment
        factory.tiles = MutableList(4){Color.INVISIBLE}
        gs.state = GameState.PLACE

        if(gs.online && gs.players[gs.currentPlayer].playerType != PlayerType.ONLINE){
            rootService.networkService.sendSelectTilesMessage(factoryIndex,color)
        }

        onAllRefreshables {
            refreshAfterTakeTilesFromFactory(color, factoryIndex, takenCount)
        }
    }


    /**
     * Legt die zuvor genommenen Spielsteine (`takenTiles`) des aktuellen Spielers
     * in die angegebene [PatternLine] auf seinem [Board]. Überschüssige Steine,
     * die nicht in die Reihe passen, werden in die `floorLine` verschoben,
     * indem placeTilesOntoFloorLine() aufgerufen wird.
     *
     * @param playerIndexed Index des Spielers
     * @param patterLineIndex Die Musterreihe, in die die Steine gelegt werden sollen.
     *
     * Vorbedingungen:
     * - Es muss der Zug des aktuellen Spielers sein (`currentPlayer` in Game).
     * - `takenTiles` darf nicht leer sein.
     * - [PatternLine] ist entweder leer oder enthält nur Steine derselben Farbe.
     * - Die entsprechende Position in der [Board.wall] darf nicht bereits einen Stein dieser Farbe enthalten.
     * - Der Spielzustand muss auf "PLACE" gesetzt sein.
     *
     * Nachbedingungen:
     * - Steine werden von rechts nach links in [PatternLine] gelegt, bis diese voll ist.
     * - `takenTiles` ist danach leer.
     * - Der Spielzustand wird auf "END_TURN" gesetzt und aufgerufen.
     *
     * @returns Diese Methode hat keinen Rückgabewert (`Unit`).
     *
     * @throws IllegalStateException, wenn keine Steine in `takenTiles` vorhanden sind,
     * es nicht der Zug des Spielers ist oder der Spielzustand falsch gesetzt wurde.
     * @throws IllegalArgumentException, wenn die Farbrestriktionen verletzt werden.
     *
     * @sample placeTilesOntoBoard(0,0)
     */
    fun placeTilesOntoBoard(playerIndexed: Int, patterLineIndex: Int) {
        val game = rootService.currentGame ?: error("No active game")
        val gs = game.azulGameState
        val patternLine = gs.players[playerIndexed].board.patternLines[patterLineIndex]
        game.redoStack.clear()
        require(gs.currentPlayer in gs.players.indices) { "It's not currently any player's turn" }
        check(gs.state == GameState.PLACE) { "Game state must be PLACE to place tiles on board" }


        val (maybeColor, takenCount) = gs.takenTiles
        require(maybeColor != Color.INVISIBLE && takenCount > 0) { "No tiles have been taken to place" }
        val takenColor: Color = maybeColor

        val player = gs.players[gs.currentPlayer]

        // Pattern line color constraint
        check(patternLine.color == Color.INVISIBLE || patternLine.color == takenColor) {
            "Pattern line contains tiles of a different color"
        }

        // Ensure the corresponding Wall row must not already have this color
        val rowIndex = player.board.patternLines.indexOf(patternLine)
        require(rowIndex >= 0) { "Pattern line is not part of the current player's board" }
        val wallRow: Array<WallTile> = player.board.wall[rowIndex]
        require(wallRow.none { it.filled && it.color == takenColor }) {
            "The corresponding wall row already has a tile of this color"
        }

        // Place as many as fit
        val capacity = patternLine.size
        val availableSpace = capacity - patternLine.fill
        val toPlace = minOf(availableSpace, takenCount)

        if (patternLine.color == Color.INVISIBLE) patternLine.color = takenColor
        patternLine.fill += toPlace
        // Handle leftovers: give them to the floorLine method

        onAllRefreshables {
            refreshAfterPlaceTilesInPatternLine( gs.currentPlayer, patterLineIndex, toPlace)
        }
        val leftover = takenCount - toPlace
        if (leftover > 0) {
            gs.takenTiles = takenColor to leftover
            placeTilesOntoFloorLine(playerIndexed,patterLineIndex)
        }else{
            println("$$$$$$$$$$$$$$$,REFRESH")
            gs.takenTiles = Color.INVISIBLE to 0
            gs.state = GameState.END_TURN
            if (gs.online)
            rootService.gameService.endTurn(patterLineIndex)
        }
    }


    /**
     * Wählt die Tiles mit der ausgewählten Farbe [color] vom [Middle] des Games.
     * Falls das erste Mal vom [Middle] gezogen wird,
     * wird der [Color.START_MARKER] in die [entity.Board.floorLine] des Spielers gelegt.
     *
     * @param color Die Farbe der ausgewählten Tiles
     *
     * Vorbedingungen:
     * - Es existiert ein Spiel.
     * - Der Spieler ist am Zug.
     * - [GameState] muss [GameState.TAKE] sein.
     * - Es muss mindestens eine Tile mit [color] sich in [Middle] befinden.
     *
     * Nachbedingungen:
     * - Alle Tiles aus der [Middle] mit [color] befinden sich in [AzulGameState.takenTiles].
     * - Die ausgewählten Tiles von [color] wurden aus [Middle] entfernt.
     * - Wenn es der erste Spieler war, befindet sich der [Color.START_MARKER] in der floorLine des Spielers.
     * - [GameState] wird auf [GameState.PLACE] gesetzt.
     *
     * @returns Unit, besitzt keinen Rückgabewert.
     *
     * @throws IllegalStateException Es gibt kein Spiel oder falscher Zustand.
     * @throws IllegalArgumentException Die ausgewählte [color] befindet sich nicht in der Mitte.
     *
     * @sample takeTilesFromMiddle(Color.YELLOW)
     */
    fun takeTilesFromMiddle(color: Color) {
        println("FROMMIDDLEEE")
        val game = rootService.currentGame ?: error("No active game")
        val gs = game.azulGameState
        game.redoStack.clear()
        require(gs.currentPlayer in gs.players.indices) { "It's not currently any player's turn" }
        check(gs.state == GameState.TAKE) { "Game state ${gs.state} must be TAKE to take tiles from middle" }
        require(color != Color.START_MARKER) { "You cannot choose START_MARKER as a color to take" }
        require(gs.middle.tiles.any { it == color }) { "The middle doesn't contain tiles of the specified color"}

        // compute what we will report to the GUI
        val takenCount  = gs.middle.tiles.count { it == color }
        val tookMarker  = gs.middle.tiles.contains(Color.START_MARKER) // vor dem Entfernen prüfen

        // gewählte Farbe aus der Mitte nehmen & speichern
        gs.takenTiles = color to takenCount
        gs.middle.tiles = gs.middle.tiles.filter { it != color } as MutableList<Color>

        // falls erster Griff: START_MARKER entfernen und versuchen, auf Floor zu legen
        if (tookMarker) {
            gs.middle.tiles = gs.middle.tiles.filter { it != Color.START_MARKER } as MutableList

            val floor = gs.players[gs.currentPlayer].board.floorLine
            var placed = false
            for (i in floor.indices) {
                if (floor[i] == Color.INVISIBLE) {
                    floor[i] = Color.START_MARKER
                    placed = true
                    break
                }
            }
        }

        if(gs.online && gs.players[gs.currentPlayer].playerType != PlayerType.ONLINE){
            rootService.networkService.sendSelectTilesMessage(-1,color)
        }

        gs.state = GameState.PLACE
        onAllRefreshables {
            refreshAfterTakeTilesFromMiddle(color)
        }
    }



    /**
     * Legt die Tiles aus [AzulGameState.takenTiles] in die [Board.floorLine] des Spielers.
     * Falls die [Board.floorLine] voll ist, werden die restlichen Tiles in [AzulGameState.usedTiles] abgelegt.
     *
     * @param playerIndexed Index des Spielers
     *
     * Vorbedingungen:
     * - Es existiert ein Spiel.
     * - Der Spieler ist am Zug.
     * - [GameState] muss [GameState.PLACE] sein.
     * - [AzulGameState.takenTiles] darf nicht leer sein.
     *
     * Nachbedingungen:
     * - [GameState] wird auf [GameState.END_TURN] gesetzt.
     * - Falls die floorLine noch nicht voll ist, wurden Tiles aus [AzulGameState.takenTiles] in die [Board.floorLine]
     *  von links nach rechts gesetzt
     * - Überschüssiges Tiles wurden in [AzulGameState.usedTiles] abgelegt.
     * - [AzulGameState.takenTiles] ist leer.
     *
     * @returns Unit, besitzt keinen Rückgabewert.
     *
     * @throws IllegalStateException Es gibt kein Spiel, falscher Zustand oder [AzulGameState.takenTiles] ist leer.
     */
    fun placeTilesOntoFloorLine(playerIndexed: Int, row: Int = -1){
        val game = rootService.currentGame ?: error("No active game")
        val gs = game.azulGameState
        game.redoStack.clear()
        check(gs.state == GameState.PLACE) { "Game state must be PLACE to place tiles onto the floor line" }
        require(gs.currentPlayer in gs.players.indices) {
            "It's not currently any player's turn"
        }

        if(playerIndexed != gs.currentPlayer) {
            throw IllegalStateException("You can only place tiles onto your own floor line")
        }

        val (color, count) = gs.takenTiles
        val player = gs.players[gs.currentPlayer]

        var placed = 0
        for (i in player.board.floorLine.indices) {
            if (player.board.floorLine[i] == Color.INVISIBLE && placed < count) {
                player.board.floorLine[i] = color
                placed++
            }
        }

        // anything left over goes into usedTiles
        val overflow = count - placed
        repeat(overflow) {
            gs.usedTiles.add(color)
        }

        //set taken tiles to 0
        gs.takenTiles = Color.INVISIBLE to 0
        onAllRefreshables { refreshAfterPlaceTilesInFloorLine(gs.currentPlayer, placed) }

        println("$$$$$$$$$$$$$$$,REFRESH")
        gs.state = GameState.END_TURN
        if (gs.online)
        rootService.gameService.endTurn(row)
    }

    /**
     * Die Methode undo() erlaubt es dem aktiven Spieler den letzten Zug rückgängig zu machen.
     * Diese Funktion ist nur im offline-Modus verwendbar.
     * Es wird der Zustand des Spiels auf den Zustands des Spiels nach Beendigung des letzten Zuges gesetzt.
     * Bei Aufruf der Funktion wird das oberste Element des Stacks gepopt und das Spiel auf diesen Zustand gesetzt.
     * Gleichzeitig wird der rückgängig gemachte Zustand auf dem redo-Stack gespeichert.
     *
     * Vorbedingungen:
     * Eine offline Spiel-Instanz muss existieren.
     * Es muss mindestens ein Zug ausgeführt worden sein.
     * Ein Spieler muss im Zug sein.
     *
     * Nachbedingungen:
     * Das Spiel befindet sich im Zustand, der in undoStack.pop() enthalten ist.
     * Der vorherige Spielzustand wird mit redoStack.push() auf den redo-Stack abgelegt.
     *
     * Parameter:
     * ---
     *
     * Gültige Wertebereiche für die Parameter:
     * ---
     *
     *Ergebnis:
     * @returns Diese Methode hat keinen Rückgabewert (`Unit`).
     *
     * @throws IllegalStateException Falls noch kein Zug ausgeführt worden ist.
     */
    fun undo(){
        println("undo")
        val game = rootService.currentGame ?: error("No active game")

        if(game.undoStack.isEmpty()){
            throw IllegalStateException("There must have been at least one turn played already")
        }

        println(Json.encodeToString(game.undoStack.peek().factories))
        val prevState = game.undoStack.pop()
        //online game check hier erstmal weg (undo/redo wird in gui disabled if online)
        println(game.azulGameState.currentPlayer)
        game.redoStack.push(prevState)
        game.azulGameState = prevState.copy()
        println(prevState.currentPlayer)
        println(game.azulGameState.currentPlayer)
        println("------------------")
        onAllRefreshables {
            refreshAfterUndo()
            refreshAfterNewTurn()
        }


    }

    /**
     * Die Methode redo() erlaubt es dem aktiven Spieler einen rückgängig gemachten Zug erneut auszuführen.
     * Diese Funktion ist nur im offline-Modus verwendbar.
     * Die rückgängiggemachten Spielzüge befinden sich auf dem redo-Stack (von undo() dort abgelegt).
     * Bei Aufruf der Funktion wird dann das oberste Element des Stacks gepopt und das Spiel auf diesen Zustand gesetzt.

     * Vorbedingungen:
     * Eine offline Spiel-Instanz muss existieren.
     * Es muss mindestens ein Zug ausgeführt worden sein.
     * Es muss mindestens ein Zug rückgängig gemacht worden sein und es wurde danach keine Aktion ausgeführt.
     * Ein Spieler muss im Zug sein.
     *
     * Nachbedingungen:
     * Das Spiel befindet sich im Zustand, der in redoStack.pop() enthalten ist.
     * Der redo-Stack wurde durch pop() um das oberste Element verkleinert.
     *
     * Parameter:
     * ---
     *
     * Gültige Wertebereiche für die Parameter:
     * ---
     *
     *Ergebnis:
     * @returns Diese Methode hat keinen Rückgabewert (`Unit`).
     *
     * @throws IllegalStateException Falls kein Zug rückgängig gemacht wurde.
     */
    fun redo(){
        println("redo")
        //deepcopy?
        val game = rootService.currentGame ?: error("No active game")

        if(game.redoStack.isEmpty()){
            throw IllegalStateException("There must have been at least one turn undone already")
        }


        val nextState = game.redoStack.pop()
        //online game check hier erstmal weg (undo/redo wird in gui disabled if online)
        println(game.azulGameState.currentPlayer)
        game.undoStack.push(nextState)
        game.azulGameState = nextState.copy()
        println(nextState.currentPlayer)
        println(game.azulGameState.currentPlayer)

        onAllRefreshables {
            refreshAfterRedo()
            refreshAfterNewTurn()
        }
    }

    /**
     * updates the speed of the bot
     */
    fun speedSet(){
        val game = rootService.currentGame ?: error("No active game")
        game.speed = (game.speed+ 500) %  9000
        if(game.speed == 0) game.speed = 500
        if (!game.azulGameState.online)
        if (game.speed<2500)game.speed = 2500
        onAllRefreshables {
            refreshAfterSpeedSet(game.speed)
        }
    }

}
