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
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


//Für endTurn row = 0 nur für network testen reingepackt müssen da einmal gucken
/**
 * Unit tests for ending a turn in Azul Game.
 */
class EndTurnTest {
    private lateinit var rootService: RootService
    private lateinit var gameService: GameService

    val board1: Board = Board(
        arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})
    val board2: Board = Board(
        arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})

    val player1 = Player("Neo", 0, board1, PlayerType.HUMAN)
    val player2 = Player("Rohan", 0, board2, PlayerType.HUMAN)

    val endTurnWrongState = AzulGameState(
        1, GameState.START_TURN, mutableListOf(player1, player2))
    val endTurnFactoriesNotEmpty = AzulGameState(
        0, GameState.END_TURN, mutableListOf(player1, player2))
    val endTurnFactoriesEmpty = AzulGameState(
        0, GameState.END_TURN, mutableListOf(player1, player2))

    /**
     * set up before each test
     */
    @BeforeEach
    fun setup(){
        rootService = RootService()
        gameService = GameService(rootService)
    }

    /**
     * Tests that an exception is thrown when trying to end a turn without an active game
     */
    @Test
    fun endTurnNoGame(){
        rootService.currentGame = null
        assertThrows<IllegalStateException> { gameService.endTurn(0) }
    }

    /**
     * Tests that an exception is thrown when trying to end a turn when not in END_TURN state
     */
    @Test
    fun endTurnWrongState(){
        rootService.currentGame = AzulGame(endTurnWrongState)
        assertThrows<IllegalStateException> { gameService.endTurn(0) }
    }

    /**
     * Tests that currentPlayer, GameState and undoStack are correctly adjusted
     * when endTurn is called with no empty middle or factories
     */
    @Test
    fun endTurnFactoriesNotEmpty(){
        endTurnFactoriesNotEmpty.factories.add(Factory(mutableListOf(Color.RED)))

        rootService.currentGame = AzulGame(endTurnFactoriesNotEmpty)

        val gamestate = rootService.currentGame?.azulGameState ?: error("No active gamestate")

        assertDoesNotThrow { gameService.endTurn(0) }

        assertEquals(GameState.START_TURN, gamestate.state)
        assertEquals(1, gamestate.currentPlayer)
    }

    /**
     * Tests that the GameState and undoStack are correctly adjusted and the currentPlayer
     * stays the same when endTurn is called with no filled middle or factories
     */
    @Test
    fun endTurnFactoriesEmpty(){
        endTurnFactoriesEmpty.factories.add(Factory())
        endTurnFactoriesEmpty.currentPlayer = 0

        rootService.currentGame = AzulGame(endTurnFactoriesEmpty)

        endTurnFactoriesEmpty.players[1].board.floorLine[0]=Color.START_MARKER
        val gamestate = rootService.currentGame?.azulGameState ?: error("No active gamestate")

        assertDoesNotThrow { gameService.endTurn(0) }

        assertEquals(GameState.START_NEW_ROUND, gamestate.state)
        assertEquals(1, gamestate.currentPlayer)
    }
}