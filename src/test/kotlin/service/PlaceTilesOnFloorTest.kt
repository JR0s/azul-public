package service

import entity.AzulGame
import entity.AzulGameState
import entity.Board
import entity.Color
import entity.Factory
import entity.GameState
import entity.PatternLine
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Unit tests for placing tiles on the floor in Azul Game.
 */
class PlaceTilesOnFloorTest {
    private lateinit var rootService: RootService
    private lateinit var playerService: PlayerActionService

    val board1: Board = Board(arrayOf(PatternLine(1)))
    val mockPlayer1 = Player("Bob", 0, board1, PlayerType.HUMAN)
    val mockPlayer2 = Player("Tim", 0, Board(arrayOf(PatternLine(1)),
        mutableListOf()), PlayerType.HUMAN)

    val mockGameWrongState = AzulGameState(1, GameState.GAME_OVER,
        mutableListOf(mockPlayer1, mockPlayer2))
    val mockGameWrongPlayerIdx = AzulGameState(2, GameState.PLACE,
        mutableListOf(mockPlayer1, mockPlayer2))
    val mockGameOK = AzulGameState(0, GameState.PLACE,
        mutableListOf(mockPlayer1, mockPlayer2)).apply {
        takenTiles = Pair(Color.YELLOW, 4)
    }

    /**
     * set up before each test
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        playerService = PlayerActionService(rootService)

    }

    /** Tests that placing tiles without a game throws an exception. */
    @Test
    fun placeTilesFloorNoGame(){
        rootService.currentGame = null
        val ex = assertThrows<IllegalStateException> {
            playerService.placeTilesOntoFloorLine(rootService.currentGame?.azulGameState?.currentPlayer ?: 0)
        }
        assertEquals("No active game", ex.message)
    }

    /** Tests that placing tiles with wrong indexed player throws an exception. */
    @Test
    fun placeTilesFloorWrongIndexedPlayer(){
        rootService.currentGame = AzulGame(mockGameOK)
        val ex = assertThrows<IllegalStateException> {
            playerService.placeTilesOntoFloorLine(1)
        }
        assertEquals("You can only place tiles onto your own floor line", ex.message)
    }

    /** Tests that placing tiles in wrong state throws an exception. */
    @Test
    fun placeTilesFloorWrongState(){
        rootService.currentGame = AzulGame(mockGameWrongState)
        val ex = assertThrows<IllegalStateException> {
            playerService.placeTilesOntoFloorLine(rootService.currentGame?.azulGameState?.currentPlayer ?: 0)
        }
        assertEquals("Game state must be PLACE to place tiles onto the floor line", ex.message)
    }

    /** Tests that placing tiles with wrong player on turn throws an exception. */
    @Test
    fun placeTilesFloorWrongPlayerIdx(){
        rootService.currentGame = AzulGame(mockGameWrongPlayerIdx)
        val ex = assertThrows<IllegalArgumentException> {
            playerService.placeTilesOntoFloorLine(rootService.currentGame?.azulGameState?.currentPlayer ?: 0)
        }
        assertEquals("It's not currently any player's turn", ex.message)
    }

    /** Tests placing tiles onto floor success. */
    @Test
    fun placeTilesOK(){
        rootService.currentGame = AzulGame(mockGameOK)
        val gs = rootService.currentGame?.azulGameState ?: error("No active game state")
        val currentPlayer = gs.players.getOrNull(gs.currentPlayer) ?: error("No current player")
        for(i in 0..4){
            currentPlayer.board.floorLine[i] = Color.BLACK
        }
        // factories dürfen nicht leer, sonst end round
        gs.factories= mutableListOf(Factory(mutableListOf(Color.BLUE,Color.RED)))
        playerService.placeTilesOntoFloorLine(rootService.currentGame?.azulGameState?.currentPlayer ?: 0)

        val expected = mutableListOf(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
            Color.BLACK, Color.YELLOW, Color.YELLOW)
        assertEquals(expected, currentPlayer.board.floorLine)
        assertEquals(mutableListOf(Color.YELLOW, Color.YELLOW), gs.usedTiles)
    }
}