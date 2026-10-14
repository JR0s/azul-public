package service

import entity.AzulGame
import entity.AzulGameState
import entity.Board
import entity.Color
import entity.Factory
import entity.GameState
import entity.Middle
import entity.PatternLine
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Test for bot moves that are being called directly from code
 */
class ApplyBotMoveTest {
    private lateinit var rootService: RootService
    private lateinit var gameService: GameService

    val board1: Board = Board(arrayOf(PatternLine(1, fill = 1)))
    val board2: Board = Board(arrayOf(PatternLine(1, fill = 0)))
    val mockPlayer1 = Player("Bob", 0, board1, PlayerType.BOT_RANDOM)
    val mockPlayer2 = Player("Tim", 0, board2, PlayerType.HUMAN)

    val mockGameOK = AzulGameState(0, GameState.TAKE,
        mutableListOf(mockPlayer1, mockPlayer2)).apply {
            factories = mutableListOf(Factory(tiles = mutableListOf(Color.YELLOW)),
                Factory(tiles = mutableListOf(Color.YELLOW))) }

    val mockGameOKMiddle = AzulGameState(1, GameState.TAKE,
        mutableListOf(mockPlayer1, mockPlayer2)).apply {
        middle = Middle(tiles = mutableListOf(Color.YELLOW))
    }

    /**
     * set up the tests
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = GameService(rootService)

    }

    /** Test Bot move for not bot gives error*/
    @Test
    fun botTestWrongPlayer(){
        rootService.currentGame = AzulGame(mockGameOK)
        val ex = assertThrows<IllegalStateException> {
            gameService.applyBotsMove(PlayerType.HUMAN)
        }
        assertEquals("Current player is not a bot", ex.message)
    }

    /** Test Bot move without active game throwsa n error*/
    @Test
    fun botTestNoGame(){
        rootService.currentGame = null
        val ex = assertThrows<IllegalStateException> {
            gameService.applyBotsMove(PlayerType.BOT_RANDOM)
        }
        assertEquals("No active game state", ex.message)
    }

    /** Test Bot move only possible is floorLine*/
    @Test
    fun randomBotTestFloor(){
        rootService.currentGame = AzulGame(mockGameOK)
        val gs= rootService.currentGame?.azulGameState ?: error("No game state")
        gameService.applyBotsMove(PlayerType.BOT_RANDOM)

        val expected = mutableListOf(Color.YELLOW, Color.INVISIBLE, Color.INVISIBLE, Color.INVISIBLE,
            Color.INVISIBLE, Color.INVISIBLE, Color.INVISIBLE)
        Thread.sleep(10000)
        assertEquals(expected, gs.players[0].board.floorLine)
    }

    /** Test Bot move only possible is floorLine*/
    @Test
    fun minMaxBotTestFloor(){
        rootService.currentGame = AzulGame(mockGameOKMiddle)
        // factories dürfen nicht leer, sonst end round
        val gs= rootService.currentGame?.azulGameState ?: error("No game state")
        gs.factories= mutableListOf(Factory(mutableListOf(Color.YELLOW,Color.RED)))
        gameService.applyBotsMove(PlayerType.BOT_PRO)
        Thread.sleep(10000)
        assertEquals(Color.YELLOW, gs.players[1].board.patternLines[0].color)
    }
}