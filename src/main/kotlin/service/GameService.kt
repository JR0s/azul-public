package service
import edu.udo.cs.sopra.ntf.ConnectionState
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import entity.*
import kotlinx.coroutines.delay
import service.bot.BotStrategy
import service.bot.MinMaxBot
import service.bot.RandomBot
import java.lang.Thread.sleep
import kotlin.collections.MutableList

/**
 * Service layer class that provides the logic for actions not directly
 * related to a single player.
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */
class GameService(private val rootService: RootService): AbstractRefreshingService() {
    /**
     * Startet ein neues Spiel mit den angegebenen Spielern.
     *
     * @param online Gibt an, ob das Spiel online gespielt wird.
     * @param players Liste der Spieler mit Namen und Spielertyp (z. B. Mensch oder KI).
     */
    fun startNewGame(online: Boolean, players: Array<Pair<String, PlayerType>>) {
        require(players.isNotEmpty()) { "Es muss mindestens einen Spieler geben" }
        val playerList = players.map { (name, type) ->
            Player(
                name = name,
                score = 0,
                board = createEmptyBoard(),
                playerType = type
            )
        }.toMutableList()

        val gameState = AzulGameState(
            currentPlayer = 0,
            state = GameState.NEW_GAME,
            players = playerList,
            online = online
        )
        Color.entries
            .filter { it !in listOf(Color.START_MARKER, Color.INVISIBLE) }
            .forEach { color -> repeat(20) { gameState.bag.add(color) } }

        gameState.bag.shuffle()

        val factoryCount = when (playerList.size) {
            2 -> 5
            3 -> 7
            4 -> 9
            else -> throw IllegalArgumentException("Ungültige Spieleranzahl: ${playerList.size}")
        }

        // Mittelablage mit Startmarker
        gameState.middle.tiles = mutableListOf()
        gameState.middle.tiles.add(Color.START_MARKER)

        // Factories befüllen
        repeat(factoryCount) {
            val factoryTiles = MutableList(4) { Color.INVISIBLE }
            gameState.factories.add(Factory(factoryTiles))
        }

        // Spielzustand direkt hier anpassen
        gameState.state = GameState.START_NEW_ROUND

        // Komplettes Spiel erstellen
        val game = AzulGame(gameState)
        rootService.currentGame = game

        onAllRefreshables { refreshAfterStartGame(playerList.size) }

        if(!gameState.online){
            startNewRound()
        }
    }

    /**
     * Hilfsfunktion: Erstellt ein leeres Spielbrett für einen Spieler.
     *
     * @return Ein neues [Board]-Objekt mit leeren Reihen und Platzhaltern.
     */
    fun createEmptyBoard(): Board {
        val patternLines = Array(5) { i -> PatternLine(i + 1,0, Color.INVISIBLE ) }

        return Board(patternLines)
    }

    /**
     * Speichert das aktuelle Spiel in einer JSON-Datei.
     *
     * @param fileName Name der Datei, in die gespeichert werden soll (muss auf .json enden).
     */
    fun saveGame(fileName: String) {

        require(fileName.isNotBlank()) { "Dateiname darf nicht leer sein" }
        require(fileName.endsWith(".json", ignoreCase = true)) { "Dateiname muss mit .json enden" }
        val game = checkNotNull(rootService.currentGame) { "Kein Spiel zum Speichern vorhanden" }

        // Undo-/Redo-Stacks leeren (sollen nicht mitgespeichert werden)
        game.undoStack.clear()
        game.redoStack.clear()

        // Sicherstellen, dass der Zielordner existiert (wird sonst angelegt)
        File(fileName).parentFile?.mkdirs()

        // Spielzustand als JSON serialisieren und in Datei schreiben
        File(fileName).writeText(Json.encodeToString(game))

        onAllRefreshables { refreshAfterSaveGame() }
    }

    /**
     * Lädt ein gespeichertes Spiel aus einer JSON-Datei.
     *
     * @param fileName Name der Datei, die geladen werden soll.
     */
    fun loadGame(fileName: String) {
        require(fileName.isNotBlank()) { "Dateiname darf nicht leer sein" }

        // Datei-Objekt erstellen
        val file = File(fileName)

        // Überprüfen: Datei muss existieren und lesbar sein
        require(file.exists()) { "Datei existiert nicht: $fileName" }
        require(file.canRead()) { "Datei kann nicht gelesen werden: $fileName" }

        // Dateiinhalt lesen und von JSON zurück in ein AzulGame-Objekt umwandeln
        rootService.currentGame = Json.decodeFromString<AzulGame>(file.readText())

        onAllRefreshables { refreshAfterLoadGame() }
    }


    /**
     * Die Methode startNewRound() startet eine neue Runde und füllt die Factories auf
     *
     * Vorbedingungen:
     * - Es muss ein aktives Spiel existieren
     * - Das Spiel muss im state "START_NEW_ROUND" sein.
     *
     * Nachbedingungen:
     * - Die Factories werden mit Tiles aus dem Bag gefüllt
     * - Wenn es nicht genug tiles gibt, um alle Factories zu füllen, werden so viele wie möglich gefüllt, dann
     * eine Factory mit den übrigen 1-3 Tiles gefüllt und alle übrigen bleiben leer
     * - Das Spiel wird auf den Zustand "START_TURN" gesetzt.
     *
     * @throws IllegalStateException, wenn die Methode aufgerufen wird, ohne dass ein Spiel existiert
     * oder der game state falsch gesetzt ist
     *
     * @sample startNewRound()
     */
    fun startNewRound(){
        val game = rootService.currentGame ?: error("No active game")
        val gs = game.azulGameState
        check((gs.state == GameState.START_NEW_ROUND || gs.state == GameState.NEW_GAME)) {
            "Game state must be START_NEW_ROUND" }

        //refill factories
        for (factory in gs.factories) {
            factory.tiles.clear() // clear existing tiles in factory
            repeat(4) {
                // draw a tile
                //FROM THE FRONT BITTE
                val tile = gs.bag.removeAt(0)
                factory.tiles.add(tile)
            }
        }

        gs.state = GameState.START_TURN
        onAllRefreshables { refreshAfterStartNewRound() }
    }

    /**
     * Die Methode startNewTurn() startet den Zug des nächsten Spielers.
     *
     * Vorbedingungen:
     * - Es muss ein aktives Spiel existieren
     * - Der Spielzustand muss auf "START_TURN" gesetzt sein
     *
     * Nachbedingungen:
     * - Der GameState wird auf "TAKE" gesetzt
     * - Der Spieler darf jetzt tiles aus einer factory oder der Mitte nehmen
     *
     * @throws IllegalStateException, wenn die Methode aufgerufen wird, ohne dass ein Spiel existiert.
     *
     * @sample startNewTurn()
     */
    fun startNewTurn(){
        val game = rootService.currentGame ?: error("No active game")
        val gs = game.azulGameState
        check(gs.state == GameState.START_TURN) { "Game state must be START_TURN" }


        gs.state = GameState.TAKE
        println("TAKEEEEEEEEEEEEETHEWHEEL")
        //deepcopy?
        game.undoStack.push(gs.copy())
        onAllRefreshables { refreshAfterNewTurn() }

        val currPlayerType = gs.players[gs.currentPlayer].playerType

        if (currPlayerType in listOf(PlayerType.BOT_RANDOM, PlayerType.BOT_PRO)){
                    applyBotsMove(currPlayerType)
        }
    }

    /**
     * Die Methode endTurn() beendet den Zug des aktuellen Spielers und bereitet den nächsten vor.
     *
     * Vorbedingungen:
     * - Es muss ein aktives Spiel existieren
     * - Der Spielzustand muss auf "END_TURN" gesetzt sein
     *
     * Nachbedingungen:
     * - Der game state wird auf "START_TURN" gesetzt, wenn es noch Tiles in der Mitte/Factories gibt
     * - Der game state wird auf "END_ROUND" gesetzt und endRound() aufgerufen
     * - Der currentPlayer wird auf den int des nächsten Spielers gesetzt
     * - Der Zug wird auf den undoStack gepusht
     *
     * @throws IllegalStateException, wenn die Methode aufgerufen wird, ohne dass ein Spiel existiert
     * oder der Spielstand nicht "END_TURN" ist
     *
     * @sample endTurn()
     */
    //floorline -1
    //patternline 0,1,...
    fun endTurn(row : Int){
        println(row)
        val game = rootService.currentGame ?: error("No active game")
        val gamestate = game.azulGameState
        check(gamestate.state == GameState.END_TURN){
            "Game state must be END_TURN"
        }
        print("444444444WIRDGEMACHT")
        var factoriesFull = true
        var middleEmpty = false
        if(gamestate.middle.tiles.isEmpty()){
            middleEmpty = true
        }
        for(factory in gamestate.factories){
            if(factory.tiles.any { it != Color.INVISIBLE }){
                factoriesFull = false
            }
        }

        if(factoriesFull && middleEmpty){
            gamestate.state = GameState.END_ROUND
            println("HÄÄÄÄÄÄÄÄÄÄÄENDROUND")
            if(gamestate.online && gamestate.players[gamestate.currentPlayer].playerType != PlayerType.ONLINE){
                rootService.networkService.sendPlaceTilesMessage(row)
            }
            rootService.networkService.endTurnNetworkStuff()
            endRound()
        }
        else{
            gamestate.state = GameState.START_TURN
            println("IHHHHHHHHHHHHSTARTTURN")
            if(gamestate.online && gamestate.players[gamestate.currentPlayer].playerType != PlayerType.ONLINE){
                rootService.networkService.sendPlaceTilesMessage(row)
            }
            rootService.networkService.endTurnNetworkStuff()

            gamestate.currentPlayer = (gamestate.currentPlayer + 1) % gamestate.players.size


            onAllRefreshables {
                refreshAfterEndTurn()
            }
            //startTurn wird dann von der GUI aufgerufen
        }

    }

    /**
     * Beendet die aktuelle Runde und bereitet entweder das Spielende oder die nächste Runde vor.
     *
     * Vorbedingungen:
     * - Das Spiel ist aktiv.
     * - Der Spielzustand muss auf [GameState.END_ROUND] gesetzt sein
     *
     * Nachbedingungen:
     * - Für alle Spieler: Tiles von vollständigen Pattern-Lines werden auf die Wall gesetzt und Punkte berechnet
     * (updateScore() wird aufgerufen)
     * - Floor-Line wird ausgewertet, Punkte abgezogen,
     *  der FirstMarker wird in die Mitte gelegt und der neue Startspieler (AzulGame.currentPlayer) gesetzt.
     *  und Tiles in den Deckel (AzulGame.usedTiles) gelegt.
     * - Pattern-Lines werden geleert.
     * - Falls ein Spieler eine vollständige Wall-Reihe (5 Tiles) hat,
     *  wird der Spielzustand auf [GameState.GAME_OVER] gesetzt und gameOver() aufgerufen.
     * - Sonst wird der Spielzustand auf [GameState.START_NEW_ROUND] gesetzt und startNewRound() aufgerufen.
     * - Falls nicht genügend Tiles in der Bag sind, wird diese mit Tiles aus usedTiles aufgefüllt
     *
     * @throws IllegalStateException wenn das Spiel nicht aktiv ist
     * @throws IllegalArgumentException wenn der Spielzustand nicht auf [GameState.END_ROUND] gesetzt wurde
     *
     * @sample endRound()
     */
    fun endRound(){
        val game = rootService.currentGame?.azulGameState
        checkNotNull(game){"Game has not been started yet."}
        require(game.state == GameState.END_ROUND){"Game state is not END_ROUND but was ${game.state}"}

        val nextPlayer = this.getStartingPlayer()
        if(nextPlayer == -1) throw IllegalStateException("No player had the starting marker.")
        game.currentPlayer = nextPlayer
        game.middle.tiles.add(Color.START_MARKER)

        this.updateScore()

        val playerHasFullRow : Boolean = game.players.any { player ->
            player.board.wall.any { row ->
                row.all { it.filled }
            }
        }
        game.players.forEach { player -> player.board.floorLine= MutableList(7){ Color.INVISIBLE }}
        if(playerHasFullRow){
            game.state = GameState.GAME_OVER
            this.gameOver()
        }
        else{
            game.state = GameState.START_NEW_ROUND

            //Prüfe wie viele tiles noch in der bag
            //weniger drin als zum Factory befüllen dann aus lid nehmen, reinpacken, senden.
            if(game.bag.size < (game.factories.size * 4)){
                println("22222222222222222SHUFFLETIME")
                val lid = game.usedTiles.toMutableList()
                println("2222222222222222${lid.size}")
                lid.shuffle()
                game.usedTiles.clear()
                game.bag.addAll(lid)
            }

            //onAllRefreshables { refreshAfterEndRound(game.currentPlayer) }

            //damit man aus dem End_Round Zustand kommt und bag wird erneut gesendet, wenn sie leer ist.
            game.state = GameState.START_NEW_ROUND
            print("ALLLAROUNDMEEEEEE")
            onAllRefreshables { refreshAfterEndRound(game.currentPlayer) }

            if(game.online){
                rootService.networkService.sendRefillBagMessage()
            }
        }

    }

    /**
    * Gibt den Index des Spielers mit dem Start Marker zurück
    *
    * Vorbedingungen:
    * - Das Spiel ist aktiv.
    * - Der Spielzustand muss auf [GameState.END_ROUND] gesetzt sein
    *
    *
    * @throws IllegalStateException wenn das Spiel nicht aktiv ist
    * @throws IllegalArgumentException wenn der Spielzustand nicht auf [GameState.END_ROUND] gesetzt wurde
    */
    private fun getStartingPlayer(): Int{
        val game = rootService.currentGame?.azulGameState
        checkNotNull(game){"Game has not been started yet."}
        require(game.state == GameState.END_ROUND){"Game state is not END_ROUND but was ${game.state}"}

        val firstTilePlayer = game.players.find{player -> player.board.floorLine.contains(Color.START_MARKER)}

        return game.players.indexOf(firstTilePlayer)
    }

    /**
     * Beendet das Spiel, wenn die Endbedingung aus den Azul-Regeln erfüllt ist.
     * Die Endabrechnung wird durchgeführt und die Punkte aktualisiert.
     *
     * Vorbedingungen:
     * - Das Spiel läuft.
     * - Der Spielzustand ist auf [GameState.GAME_OVER] gesetzt,
     *  (ein Spieler hat eine horizontale Reihe von 5 Tiles in wall)
     *
     * Nachbedingungen:
     * - updateScore() wurde aufgerufen, um die Punkte anzupassen
     *
     * @throws IllegalStateException wenn das Spiel nicht läuft.
     * @throws IllegalArgumentException wenn der Spielzustand nicht auf [GameState.GAME_OVER] gesetzt wurde.
     *
     * @sample gameOver()
     */
    fun gameOver(){
        val game = rootService.currentGame?.azulGameState
        checkNotNull(game){"Game has not been started yet."}
        require(game.state == GameState.GAME_OVER){"Game state is not GAME_OVER but was ${game.state}"}

        this.updateScore()
        onAllRefreshables { refreshAfterGameOver() }
    }


    /**
     * Aktualisiert die Punkte eines Spielers gemäß den Azul-Regeln.
     *
     * Vorbedingungen:
     * - Das Spiel ist aktiv.
     * - Der Spielzustand wurde auf [GameState.END_ROUND] oder [GameState.GAME_OVER] gesetzt
     *
     * Nachbedingungen:
     * - Punkte werden für zusammenhängende Tiles in horizontaler und vertikaler Richtung
     *   von der neu gesetzten Tile auf der Wall vergeben, der jeweilige player score wird aktualisiert
     * - Minuspunkte für Tiles in der Floor line werden abgezogen, der jeweilige player score wird aktualisiert
     * - Wenn der Spielzustand "GAME_OVER" war, dann wird die Endabrechnung durchgeführt.
     *
     * @throws IllegalStateException wenn das Spiel nicht aktiv ist
     * @throws IllegalStateException wenn das eine Reihe als voll angezeigt wird, ohne voll zu sein
     * @throws IllegalArgumentException wenn der Spielzustand falsch gesetzt ist
     *
     * @sample updateScore()
     */
    private fun updateScore(){
        val game = rootService.currentGame?.azulGameState
        checkNotNull(game){"Game has not been started yet."}
        require(game.state == GameState.END_ROUND || game.state == GameState.GAME_OVER){
            "Game state is not END_ROUND nor GAME_OVER but was ${game.state}"}

        // Calculates score after each round
        if(game.state == GameState.END_ROUND){
            for(player in game.players) {
                this.scoreBoard(player)
            }
        }

        // Case game.state == GameState.GAME_OVER:
        // Calculates bonus points after the game has ended
        else{
            for(player in game.players){
                this.scoreBonus(player)
            }
        }

    }

    /**
     * method for scoring the board of a given player
     */
    private fun scoreBoard(player:Player){
        val game = rootService.currentGame?.azulGameState
        checkNotNull(game){"Game has not been started yet."}

        var newScore = 0
        // score plus points for the player
        for(patternLine in player.board.patternLines.filter{it.size == it.fill}) {
            val lineColor = patternLine.color
            val lineIndex = player.board.patternLines.indexOf(patternLine)
            repeat(patternLine.size - 1) { game.usedTiles.add(lineColor) }
            player.board.wall[lineIndex].find { tile -> tile.color == lineColor }?.filled = true
            newScore = newScore + this.getScore(yPos = lineIndex, tileColor = lineColor, player = player)
            patternLine.color = Color.INVISIBLE
            patternLine.fill = 0
        }

        // count number of tiles in floorLine
        val amount = player.board.floorLine.count{it != Color.INVISIBLE}

        //score minus points of the player
        when(amount){
            1 -> newScore -=1
            2 -> newScore -=2
            3 -> newScore -=4
            4 -> newScore -=6
            5 -> newScore -=8
            6 -> newScore -=11
            7 -> newScore -=14
            else -> Unit
        }
        for(element in player.board.floorLine.filter{it != Color.INVISIBLE&& it!= Color.START_MARKER}){
            game.usedTiles.add(element)
        }
        player.score += newScore
        if(player.score < 0) player.score = 0
    }


    /**
     * method for scoring the bonus points of a given player
     */
    private fun scoreBonus(player:Player){
        val game = rootService.currentGame?.azulGameState
        checkNotNull(game){"Game has not been started yet."}

        var newScore = 0

                // full rows
                for(line in player.board.wall){
                    if(line.all{ it.filled}){
                        newScore += 2
                    }
                }

                // full columns
                for(i in 0..4){
                    val ithRow : List<WallTile> = player.board.wall.map{ row -> row[i] }
                    if(ithRow.all{ it.filled}){
                        newScore += 7
                    }
                }

                // full colors
                for(color in arrayOf(Color.RED, Color.BLUE, Color.YELLOW, Color.WHITE, Color.BLACK)){
                    val wallColor : List<WallTile> = player.board.wall.flatten().filter{it.color == color}
                    if(wallColor.all{it.filled}){
                        newScore += 10
                    }
                }

                player.score += newScore
            }



    /**
     *  Calculates the score for a given tile for each row and column
     */
    private fun getScore(yPos: Int, tileColor: Color, player:Player):Int{
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

        if(yPoints == 0 && xPoints == 0) return 1

        return xPoints+yPoints
    }

    /**
     * Funktion die einen Bot move ausführt
     */
    fun applyBotsMove(playerType: PlayerType) {
        val gs = rootService.currentGame?.azulGameState ?: error("No active game state")
        val currentPlayerIdx = gs.currentPlayer


        val bot: BotStrategy = when (playerType) {
            PlayerType.BOT_RANDOM -> RandomBot(gs)
            PlayerType.BOT_PRO  -> MinMaxBot(gs)
            else -> error("Current player is not a bot")
        }

        val move = bot.chooseMove()




        Thread{
            try {
             sleep((checkNotNull(rootService.currentGame).speed/2).toLong())

                if (move.factory == -1){
                    rootService.playerActionService.takeTilesFromMiddle(move.color)
                }
                else{
                    rootService.playerActionService.takeTilesFromFactory(move.factory, move.color)
                }



            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()

        Thread{

            sleep((checkNotNull(rootService.currentGame).speed).toLong())

            if (move.patternLine == -1){
                rootService.playerActionService.placeTilesOntoFloorLine(currentPlayerIdx)
            }
            else{
                rootService.playerActionService.placeTilesOntoBoard(currentPlayerIdx,
                    move.patternLine)
            }


        }.start()



    }
}



