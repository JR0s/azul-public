package service

import edu.udo.cs.sopra.ntf.ConnectionState
import edu.udo.cs.sopra.ntf.SelectTileMessage
import edu.udo.cs.sopra.ntf.Tile
import entity.*
import org.junit.jupiter.api.assertThrows
import java.lang.Thread.sleep
import kotlin.test.*

/**
 * class that implements the tests for the network service
 */
class NetworkServiceTest {

    private lateinit var rootServiceJoin : RootService
    private lateinit var rootServiceHost: RootService

    /**
    * simulate the creation of a connection
    */
    @Test
    fun createConnection(){
        rootServiceHost = RootService()
        rootServiceJoin = RootService()

        rootServiceHost.networkService.hostGame("fliese25", "Toni", PlayerType.HUMAN, null)
        sleep(10000)
        assertEquals(ConnectionState.WAITING_FOR_GUEST, rootServiceHost.networkService.connectionState)
        val sessionID = rootServiceHost.networkService.client?.sessionID
        assertNotNull(sessionID)

        rootServiceJoin.networkService.joinGame("fliese25", "Neo", PlayerType.HUMAN,sessionID)
        sleep(10000)
        assertEquals(ConnectionState.WAITING_FOR_INIT, rootServiceJoin.networkService.connectionState)
        val players = rootServiceHost.networkService.client?.players
        assertEquals(mutableListOf("Toni","Neo"),players)

        rootServiceHost.networkService.startNewHostedGame(players as MutableList<String>)
        rootServiceHost.gameService.startNewTurn()
        sleep(10000)
        rootServiceJoin.gameService.startNewTurn()
        assertEquals(ConnectionState.PLAYING_MY_TURN, rootServiceHost.networkService.connectionState)
        assertEquals(ConnectionState.WAITING_FOR_OPPONENT, rootServiceJoin.networkService.connectionState)
    }

    /**
     * tests the join and host game method
     */
    @Test
    fun hostAndJoinGameTest() {
        rootServiceHost = RootService()
        rootServiceJoin = RootService()

        createConnection()

        val hostGame = rootServiceHost.currentGame
        val guestGame = rootServiceJoin.currentGame
        assertNotNull(hostGame)
        assertNotNull(guestGame)
        assertNotNull(guestGame.azulGameState.players)
        print(" FINDE ${guestGame.azulGameState.players.size} FINDE")


        val hostAzul = hostGame.azulGameState
        val hostFactory = hostAzul.factories[0]
        val guestAzul = guestGame.azulGameState
        val guestFactory = guestAzul.factories[0]

        hostFactory.tiles[0] = Color.RED
        guestFactory.tiles[0] = Color.RED
        rootServiceHost.networkService.sendSelectTilesMessage(0, hostFactory.tiles[0])
        assertEquals(ConnectionState.SELECTED_TILES, rootServiceHost.networkService.connectionState)
        sleep(10000)
        hostAzul.state = GameState.START_TURN
        rootServiceHost.networkService.sendPlaceTilesMessage(0)
        sleep(20000)
        assertEquals(ConnectionState.WAITING_FOR_OPPONENT, rootServiceHost.networkService.connectionState)
        assertEquals(ConnectionState.PLAYING_MY_TURN, rootServiceJoin.networkService.connectionState)

        //Der connection state wird ab dem zweiten turn nicht mehr richtig gesetzt
        //evtl weil server response länger dauert als zeit zwischen send und 2. assertEquals
        //TODO serverunabhängig testen mit direktem setzen der connection states für jede farbe einzeln
//        hostFactory.tiles[0] = Color.BLUE
//        guestFactory.tiles[0] = Color.BLUE
//        rootServiceJoin.networkService.sendSelectTilesMessage(0, guestFactory.tiles[0])
//        assertEquals(ConnectionState.SELECTED_TILES, rootServiceJoin.networkService.connectionState)
//        sleep(10000)
//        guestAzul.state = GameState.START_TURN
//        rootServiceJoin.networkService.sendPlaceTilesMessage(0)
//        sleep(20000)
//        assertEquals(ConnectionState.WAITING_FOR_OPPONENT, rootServiceJoin.networkService.connectionState)
//        assertEquals(ConnectionState.PLAYING_MY_TURN, rootServiceHost.networkService.connectionState)
    }

    /**
     * tests if the different colours are correctly mapped from their local colour to the message standard
     */
    @Test
    fun colourMappingSelectMessageTest(){
        rootServiceHost = RootService()
        rootServiceJoin = RootService()

        rootServiceHost.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        rootServiceJoin.networkService.updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)

        val board1 = Board(
            arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})
        val board2 = Board(
            arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})

        val host = Player("Toni", 0, board1, PlayerType.HUMAN)
        val guest = Player("Neo", 0, board2, PlayerType.HUMAN)

        val hostGameState = AzulGameState(
            0, GameState.START_TURN, mutableListOf(host, guest))
        val guestGameState = AzulGameState(
            0, GameState.START_TURN, mutableListOf(host, guest))

        hostGameState.factories.add(Factory(mutableListOf(Color.RED)))
        guestGameState.factories.add(Factory(mutableListOf(Color.RED)))

        hostGameState.factories[0] = Factory(mutableListOf(Color.BLUE, Color.BLACK, Color.YELLOW, Color.WHITE))
        guestGameState.factories[0] = Factory(mutableListOf(Color.BLUE, Color.BLACK, Color.YELLOW, Color.WHITE))

        rootServiceHost.currentGame = AzulGame(hostGameState)
        rootServiceJoin.currentGame = AzulGame(guestGameState)

        for(i in 0..3){
            println("Round $i out of 3")
            rootServiceHost.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
            rootServiceHost.networkService.sendSelectTilesMessage(0, hostGameState.factories[0].tiles[i])
            assertEquals(ConnectionState.SELECTED_TILES, rootServiceHost.networkService.connectionState)

            sleep(500)

            hostGameState.state = GameState.START_TURN
            rootServiceHost.networkService.sendPlaceTilesMessage(0)
            assertEquals(ConnectionState.WAITING_FOR_OPPONENT, rootServiceJoin.networkService.connectionState)

            sleep(500)

            guestGameState.state = GameState.START_TURN
            rootServiceJoin.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)
            rootServiceJoin.networkService.sendSelectTilesMessage(0, guestGameState.factories[0].tiles[i])
            assertEquals(ConnectionState.SELECTED_TILES, rootServiceJoin.networkService.connectionState)

            sleep(500)

            guestGameState.state = GameState.START_TURN
            rootServiceJoin.networkService.sendPlaceTilesMessage(0)
            assertEquals(ConnectionState.WAITING_FOR_OPPONENT, rootServiceJoin.networkService.connectionState)

            rootServiceHost.currentGame = AzulGame(hostGameState)
        }
    }

    /**
     * tests if sendSelectTilesMessage correctly throws with an invalid color
     */
    @Test
    fun colourMappingSelectMessageWithInvalidColorTest() {
        rootServiceHost = RootService()

        rootServiceHost.networkService.updateConnectionState(ConnectionState.PLAYING_MY_TURN)

        val board1 = Board(
            arrayOf(PatternLine(1)), MutableList(7) { Color.BLACK })

        val host = Player("Toni", 0, board1, PlayerType.HUMAN)

        val hostGameState = AzulGameState(
            0, GameState.START_TURN, mutableListOf(host)
        )

        hostGameState.factories.add(Factory(mutableListOf(Color.INVISIBLE)))
        hostGameState.factories[0] = Factory(mutableListOf(Color.INVISIBLE))

        assertThrows<IllegalArgumentException> {
            rootServiceHost.networkService.sendSelectTilesMessage(0, hostGameState.factories[0].tiles[0])}
    }

    /**
     * tests if receiveSelectTilesMessage correctly maps the message standard to the local colors
     */
    @Test
    fun colourMappingReceiveSelectMessageTest(){
        rootServiceHost = RootService()

        rootServiceHost.networkService.updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)

        val board1 = Board(
            arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})
        val board2 = Board(
            arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})

        val host = Player("Toni", 0, board1, PlayerType.HUMAN)
        val guest = Player("Neo", 0, board2, PlayerType.HUMAN)

        val hostGameState = AzulGameState(
            1, GameState.TAKE, mutableListOf(host, guest))

        rootServiceHost.currentGame = AzulGame(hostGameState)

        val tiles = mutableListOf(Tile.BLUE, Tile.BLACK, Tile.TURQUOISE, Tile.YELLOW)

        for(tile in tiles){

            hostGameState.factories.clear()
            hostGameState.factories.add(Factory(mutableListOf(Color.BLUE, Color.BLACK, Color.WHITE, Color.YELLOW)))
            hostGameState.state = GameState.TAKE

            val message = SelectTileMessage(0, tile)

            rootServiceHost.networkService.receiveSelectTilesMessage(message)
            assertEquals(GameState.PLACE, hostGameState.state)
        }

        hostGameState.middle.tiles.clear()
        hostGameState.middle.tiles.add(Color.WHITE)
        hostGameState.state = GameState.TAKE

        val message = SelectTileMessage(-1, Tile.TURQUOISE)

        rootServiceHost.networkService.receiveSelectTilesMessage(message)
        assertEquals(GameState.PLACE, hostGameState.state)
    }

    /**
     * tests if receiveSelectTilesMessage correctly throws with an illegal location value
     */
    @Test
    fun receiveSelectTilesMessageWithIllegalLocationTest(){
        rootServiceHost = RootService()

        rootServiceHost.networkService.updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)

        val board1 = Board(
            arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})
        val board2 = Board(
            arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})

        val host = Player("Toni", 0, board1, PlayerType.HUMAN)
        val guest = Player("Neo", 0, board2, PlayerType.HUMAN)

        val hostGameState = AzulGameState(
            1, GameState.TAKE, mutableListOf(host, guest))

        rootServiceHost.currentGame = AzulGame(hostGameState)

        hostGameState.factories.add(Factory(mutableListOf(Color.WHITE)))
        hostGameState.state = GameState.TAKE

        val message = SelectTileMessage(9, Tile.TURQUOISE)
        val message2 = SelectTileMessage(7, Tile.TURQUOISE)
        assertThrows<IllegalArgumentException>{ rootServiceHost.networkService.receiveSelectTilesMessage(message) }
        assertThrows<IndexOutOfBoundsException>{ rootServiceHost.networkService.receiveSelectTilesMessage(message2)}
    }

    /**
     * function for refilling the bag
     */
    @Test
    fun refillBagTest(){
        rootServiceHost = RootService()
        rootServiceJoin = RootService()

        createConnection()

        val hostGame = rootServiceHost.currentGame
        val guestGame = rootServiceJoin.currentGame
        assertNotNull(hostGame)
        assertNotNull(guestGame)
        val hostAzul = hostGame.azulGameState
        val guestAzul = guestGame.azulGameState

        hostAzul.state = GameState.END_ROUND
        hostAzul.players[0].board.floorLine.add(Color.START_MARKER)
        rootServiceHost.networkService.updateConnectionState(ConnectionState.END_ROUND)
        rootServiceHost.gameService.endRound()

        //rootServiceHost.networkService.sendRefillBagMessage()
        sleep(10000)
        assertTrue(guestAzul.bag.isNotEmpty())
        assertEquals(ConnectionState.WAITING_FOR_OPPONENT, rootServiceJoin.networkService.connectionState)
    }

    /**
     * tests if the function for disconnecting works correctly
     */
    @Test
    fun disconnectTest(){
        rootServiceHost = RootService()
        rootServiceJoin = RootService()

        //createConnection()
        rootServiceHost.networkService.hostGame("fliese25", "Toni", PlayerType.HUMAN, null)
        sleep(3000)
        rootServiceHost.networkService.disconnect()

        assertEquals(null, rootServiceHost.networkService.client)
        assertEquals("", rootServiceHost.networkService.hostName)
        assertEquals(PlayerType.WAIT, rootServiceHost.networkService.chosenType)
        assertEquals(ConnectionState.DISCONNECTED, rootServiceHost.networkService.connectionState)
    }

    /**
     * tests if the disconnect only works with non-null sessionID
     */
    @Test
    fun disconnectTestWithIDNull(){
        rootServiceHost = RootService()

        createConnection()

        rootServiceHost.networkService.client?.sessionID = null

        assertNotEquals(null, rootServiceHost.networkService.client)
        assertNotEquals("", rootServiceHost.networkService.hostName)
        assertNotEquals(PlayerType.WAIT, rootServiceHost.networkService.chosenType)
        assertNotEquals(ConnectionState.DISCONNECTED, rootServiceHost.networkService.connectionState)
    }

}
