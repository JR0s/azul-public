package service.network

import edu.udo.cs.sopra.ntf.ConnectionState
import edu.udo.cs.sopra.ntf.*
import entity.*
import service.AbstractRefreshingService
import service.RootService

/**
 * NetworkService-Layer die den Umgang mit Nachrichten in online game behandelt.
 * Verbindet den Client mit den Service-Methoden.
 *
 * @param rootService
 */
class NetworkService(private val rootService: RootService) : AbstractRefreshingService(){

    var client : AzulNetworkClient? = null
        private set

    var hostName : String = ""
        private set

    var chosenType : PlayerType = PlayerType.WAIT
        private set

    var connectionState : ConnectionState = ConnectionState.DISCONNECTED
        private set

    var helpName : String = ""
        private set
    /**
     * Erstellt eine neue Spiel-Session und verbindet sich mit dem Server.
     *
     * @param secret Network secret
     * @param name Spielername
     * @param playerType Ob Bot oder nicht
     * @param sessionID ID für die Session
     */
    fun hostGame(secret : String, name : String, playerType: PlayerType, sessionID : String?){
        val connected = connect(secret,name)
        if(!connected) error("Connection failed")

        updateConnectionState(ConnectionState.CONNECTED)

        this.hostName = name
        this.chosenType = playerType

        if (sessionID.isNullOrBlank()) {
            client?.createGame(GAME_ID, "Welcome to Azul!")
        } else {
            client?.createGame(GAME_ID, sessionID, "Welcome to Azul!")
        }
        updateConnectionState(ConnectionState.WAITING_FOR_HOST)
    }

    /**
     * Joint einer Spiel-Session über die [sessionID] und verbindet sich mit dem Server.
     *
     * @param secret Network secret
     * @param name Spielername
     * @param playerType Ob Bot oder nicht
     * @param sessionID ID für die Session
     */
    fun joinGame(secret : String, name : String, playerType: PlayerType, sessionID : String){
        val connected = connect(secret,name)
        if(!connected) error("Connection failed")

        this.chosenType = playerType

        updateConnectionState(ConnectionState.CONNECTED)

        client?.joinGame(sessionID,"Hi,it's $name")

        updateConnectionState(ConnectionState.WAITING_FOR_JOIN)
    }

    /**
     * Hilfsmethode für GUI, damit Namen aktualisiert werden in der Scene.
     */
    fun addNetworkPlayer(playerNames: List<String>){
        onAllRefreshables { refreshAfterNetworkPlayerJoined(playerNames)}
    }

    /**
     * Startet ein neues Spiel beim Host mit startNewGame().
     * Wurde vom Client Auto aufgerufen, wenn 4 Spieler gejoint sind, sonst von GUI.
     * Nimmt die generierte Bag und übersetzt diese für die Network Kommunikation.
     * Sendet die [InitMessage] an die Gastspieler und falls der Host am Zug ist, wird der Zustand
     * entsprechend gesetzt.
     *
     * @param playerNames Liste der teilnehmenden Spieler
     */
    fun startNewHostedGame(playerNames : MutableList<String>){
        check(connectionState == ConnectionState.WAITING_FOR_GUEST)
        //2-4 Spieler erlaubt
        require(playerNames.size in 2..4)

        val onlinePlayers = playerNames.map{name ->
            if(name == this.hostName){
                name to chosenType
            }else{
                name to PlayerType.ONLINE
            }}.toTypedArray()

        rootService.gameService.startNewGame(true,onlinePlayers)
        val game = rootService.currentGame
        checkNotNull(game)
        val azul = game.azulGameState

        //Übersetzen der Bag vllt private methode raus machen
        val bag = azul.bag.mapNotNull{ color -> when(color){
            Color.YELLOW -> Tile.YELLOW
            Color.RED -> Tile.RED
            Color.BLUE -> Tile.BLUE
            Color.BLACK -> Tile.BLACK
            Color.WHITE -> Tile.TURQUOISE
            else ->  null }}

        val message = InitMessage(playerNames as List<String>,bag)

        val playerIdx = azul.currentPlayer
        if(azul.players[playerIdx].name == hostName){
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }else{
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }
        client?.sendGameActionMessage(message)

        rootService.gameService.startNewRound()
    }

    /**
     * Wird aufgerufen, wenn eine Init Message eingegangen ist und vom Client weitergeleitet wurde.
     * Initialisiert ein neues Spiel, mit den Informationen aus der [InitMessage] die vom Host gesendet wurde.
     * Falls der Spieler am Zug ist, wird in [ConnectionState.PLAYING_MY_TURN] gewechselt sonst in
     * [ConnectionState.WAITING_FOR_OPPONENT].
     *
     * @param message die eingegange [InitMessage]
     * @param playerName eigener Spielername (benötigt um zu checken ob am Zug oder nicht)
     */
    fun startNewJoinedGame(message : InitMessage, playerName : String){
        val onlinePlayers = message.playerNames.map{name ->
            if(name == playerName){
                name to chosenType
            }else{
                name to PlayerType.ONLINE
            }}.toTypedArray()

        val bag = message.bag.map{ tile -> when(tile){
            Tile.YELLOW -> Color.YELLOW
            Tile.RED -> Color.RED
            Tile.BLUE -> Color.BLUE
            Tile.BLACK -> Color.BLACK
            Tile.TURQUOISE -> Color.WHITE
        } }
        //Erstellt Entity für ein neues Spiel
        createJoinGame(onlinePlayers, bag as MutableList<Color>)
        //bin ich am zug oder nicht herausfinden
        val game =  rootService.currentGame
        checkNotNull(game)
        val azul = game.azulGameState
        if(azul.players[azul.currentPlayer].name == playerName){
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }else {
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }
        rootService.gameService.startNewRound()
    }

    /**
     * Hilfsmethode um die Game Entity zu erstellen, 1 zu 1 zu startNewGame nur das die übergebene Bag benutzt wird.
     */
    private fun createJoinGame(onlinePlayers : Array<Pair<String, PlayerType>>, bag : MutableList<Color>){
        val playerList = onlinePlayers.map { (name, type) ->
            Player(
                name = name,
                score = 0,
                board = rootService.gameService.createEmptyBoard(),
                playerType = type
            )
        }.toMutableList()

        val gameState = AzulGameState(
            currentPlayer = 0,
            state = GameState.NEW_GAME,
            players = playerList,
            online = true
        )

        gameState.bag = bag

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
    }

    /**
     * Erstellt einen neuen Client und verbindet sich mit dem Server.
     * Bei erfolgreicher Verbindung wird true zurückgegeben.
     *
     * @param secret Network secret
     * @param name Spielername
     */
    private fun connect(secret : String, name : String) : Boolean{
        require(connectionState == ConnectionState.DISCONNECTED && client == null)
        { "already connected to another game" }

        require(secret.isNotBlank()) { "server secret must be given" }
        require(name.isNotBlank()) { "player name must be given" }

        val newClient =
            AzulNetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                secret = secret,
                networkService = this
            )

        return if (newClient.connect()) {
            this.client = newClient
            true
        } else {
            false
        }
    }

    /**
     * Trennt die Verbindung zwischen [client] und Server und setzt den Client zurück.
     * [connectionState] wird ebenfalls wieder auf [ConnectionState.DISCONNECTED] gesetzt.
     */
    fun disconnect(){
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        hostName = ""
        helpName = ""
        chosenType = PlayerType.WAIT
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * send a [SelectTileMessage] to the opponents
     *
     * @param location resembles one of the factories (0-8) or the middle (-1)
     * @param color is the color of the tile selected by the player
     *
     * @throws IllegalArgumentException if the color selected by the player is not one of the playable tile colors
     */
    fun sendSelectTilesMessage(location : Int, color : Color){
        require(connectionState == ConnectionState.PLAYING_MY_TURN) { "not my turn" }

        val newColor = when(color){
            Color.YELLOW -> Tile.YELLOW
            Color.RED -> Tile.RED
            Color.BLUE -> Tile.BLUE
            Color.BLACK -> Tile.BLACK
            Color.WHITE -> Tile.TURQUOISE
            else -> throw IllegalArgumentException("Color must be a playable tile color")
        }

        val message = SelectTileMessage(location, newColor)
        client?.sendGameActionMessage(message)

        updateConnectionState(ConnectionState.SELECTED_TILES)
    }

    /**
     * play the opponent's turn by handling the [SelectTileMessage] sent through the server.
     *
     * @param message the message to handle
     *
     * @throws IllegalStateException if there is no game currently active
     * @throws IllegalArgumentException if the location sent in the [SelectTileMessage] is invalid
     */
    fun receiveSelectTilesMessage(message : SelectTileMessage){
        val game = rootService.currentGame
        checkNotNull(game)
        val azul = game.azulGameState

        val newColor = when(message.color){
            Tile.YELLOW -> Color.YELLOW
            Tile.RED -> Color.RED
            Tile.BLUE -> Color.BLUE
            Tile.BLACK -> Color.BLACK
            Tile.TURQUOISE -> Color.WHITE
        }

        if(message.location == -1){
            rootService.playerActionService.takeTilesFromMiddle(newColor)
        }
        else if(message.location in 0..8){
            rootService.playerActionService.takeTilesFromFactory(message.location, newColor)
        }
        else{
            throw IllegalArgumentException("Message location was invalid (must be -1, 0, 1, 2, ..., 7 or 8")
        }
    }

    /**
     * send a [PlaceTilesMessage] to the opponents
     *
     * @param row resembles the line on the player's board (0-4 for pattern lines, -1 for the floor line)
     *
     * @throws IllegalArgumentException if the [ConnectionState] is not SELECTED_TILES
     * @throws IllegalStateException if the [GameState] after placing the tiles is not END_ROUND or START_TURN
     */
    fun sendPlaceTilesMessage(row : Int){
        require(connectionState == ConnectionState.SELECTED_TILES) { "no tiles selected" }

        val game = rootService.currentGame
        checkNotNull(game)
        val azul = game.azulGameState

        val message = PlaceTilesMessage(row)
        client?.sendGameActionMessage(message)

        if(azul.state == GameState.END_ROUND){
            updateConnectionState(ConnectionState.END_ROUND)
        }
        else if(azul.state == GameState.START_TURN){
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }
        else{
            throw IllegalStateException("Game state must be END_ROUND or START_TURN")
        }
    }

    /**
     * plays the opponent's turn by handling the [PlaceTilesMessage] sent through the server
     *
     * @param message the message to handle
     *
     * @throws IllegalStateException if there is no game currently active or if the [GameState]
     * after playing the turn is invalid
     * @throws IllegalArgumentException if the row sent in the [PlaceTilesMessage] is invalid
     */
    //Änderung brauche den sender namen und den playernamen
    fun receivePlaceTilesMessage(message: PlaceTilesMessage, sender : String, playerName: String){
        val game = rootService.currentGame
        checkNotNull(game)
        val azul = game.azulGameState

        this.helpName = playerName

        val playerIndex = azul.players.indexOfFirst{it.name == sender}
        if(message.row == -1){
            rootService.playerActionService.placeTilesOntoFloorLine(playerIndex)
            println("%%%%%%%%%%%%%%%%%%%%%%%%")
        }
        else if(message.row in 0..4){
            rootService.playerActionService.placeTilesOntoBoard(playerIndex, message.row)
            println("%%%%%%%%%%%%%%%%%%%%%%%%")
            //for testing
            //rootService.gameService.endTurn(message.row)
        }
        else{
            throw IllegalArgumentException("Message row must be -1, 0, 1, 2, 3 or 4")
        }
    }
    /**
     * Updatet den Connection State nach einem Zug je nach GameState
     */
    fun endTurnNetworkStuff(){
        val game = rootService.currentGame
        checkNotNull(game)
        val azul = game.azulGameState

        val nextPlayerInt = (azul.currentPlayer + 1) % azul.players.size

        if(azul.state == GameState.END_ROUND){
            rootService.networkService.updateConnectionState(ConnectionState.END_ROUND)
        }
        else if(azul.state == GameState.START_TURN){
            if(azul.players[nextPlayerInt].name == helpName){
                rootService.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
            }else{
                rootService.networkService.updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
            }
        }
        else{
            throw IllegalStateException("Game state ${azul.state} must be END_ROUND or START_TURN")
        }
    }

    /**
     * Sendet eine [RefillBagMessage] an die anderen Spieler, aber nur wenn man der Host ist.
     * [ConnectionState] wird je, nachdem gesetzt, ob der Host am Zug ist oder nicht.
     */
    fun sendRefillBagMessage(){
        require(connectionState == ConnectionState.END_ROUND) { "not end of round" }
        val game = rootService.currentGame
        checkNotNull(game)
        val azul = game.azulGameState

        if(hostName.isNotBlank()){
            val bag = azul.bag.mapNotNull{ color -> when(color){
                Color.YELLOW -> Tile.YELLOW
                Color.RED -> Tile.RED
                Color.BLUE -> Tile.BLUE
                Color.BLACK -> Tile.BLACK
                Color.WHITE -> Tile.TURQUOISE
                else ->  null }}

            val message = RefillBagMessage(bag)

            client?.sendGameActionMessage(message)

            if(azul.players[azul.currentPlayer].playerType != PlayerType.ONLINE){
                updateConnectionState(ConnectionState.PLAYING_MY_TURN)
            }else{
                updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
            }
            println("&&&&&&&&&&&&&&&&&&&STARTNEWROUND")
            rootService.gameService.startNewRound()
        }
    }

    /**
     * Reaktion auf eine [RefillBagMessage] die vom Host gesendet wurde.
     * Spielinterne Bag wird auf die gesendete Bag gesetzt und dann startNewRound aufgerufen um die factories zu
     * befüllen.
     * [ConnectionState] wird je, nachdem gesetzt, ob der Spieler am Zug ist oder nicht.
     *
     * @param message enthält die neu aufgefüllte Bag
     * @param playerName eigener Spielername
     */
    fun receiveRefillBagMessage(message: RefillBagMessage, playerName : String){
        val game = rootService.currentGame
        checkNotNull(game)
        val azul = game.azulGameState

        val bag = message.bag.map{ tile -> when(tile){
            Tile.YELLOW -> Color.YELLOW
            Tile.RED -> Color.RED
            Tile.BLUE -> Color.BLUE
            Tile.BLACK -> Color.BLACK
            Tile.TURQUOISE -> Color.WHITE
        } }

        //Spiel interne Bag updaten
        azul.bag = bag as MutableList<Color>
        //Bin ich am Zug oder nicht
        if(azul.players[azul.currentPlayer].name == playerName){
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }else{
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }

        rootService.gameService.startNewRound()
    }

    /**
     * Hilfsmethode die [connectionState] auf [newState] updatet.
     */
    fun updateConnectionState(newState : ConnectionState){
        this.connectionState = newState
    }

    /**
     * Companion mit Serveradresse und Game-ID
     */
    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Azul"
    }
}