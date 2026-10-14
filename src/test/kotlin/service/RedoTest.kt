package service

import entity.AzulGame
import entity.AzulGameState
import entity.Board
import entity.Color
import entity.GameState
import entity.PatternLine
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Unit tests for redoing a turn in Azul Game.
 */
class RedoTest {
    private lateinit var rootService: RootService
    private lateinit var playerActionService: PlayerActionService

    val board1: Board = Board(
        arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})
    val board2: Board = Board(
        arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})

    val player1 = Player("Neo", 0, board1, PlayerType.HUMAN)
    val player2 = Player("Rohan", 0, board2, PlayerType.HUMAN)

    val redoWorks = AzulGameState(
        0, GameState.START_TURN, mutableListOf(player1, player2))

    /**
     * set up before each test
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        playerActionService = PlayerActionService(rootService)
    }

    /**
     * Tests that an exception is thrown when trying to redo a turn with no current game active
     */
    @Test
    fun redoNoGame(){
        rootService.currentGame = null
        assertThrows<IllegalStateException>{ playerActionService.redo() }
    }

    /**
     * Tests if the turn is correctly redone by "simulating" a turn, undoing it and checking
     * if the stacks are correctly adjusted
     */
    @Test
    fun redoWorks(){
        rootService.currentGame = AzulGame(redoWorks)
        val game = rootService.currentGame
        checkNotNull(game)
        val gamestate = game.azulGameState

        val redoState = gamestate
        redoState.currentPlayer = 1
        redoState.state = GameState.END_TURN
        game.redoStack.push(redoState)
        game.undoStack.push(gamestate)

        assertDoesNotThrow{playerActionService.redo()}

        val currentState = rootService.currentGame?.azulGameState ?: error("No active gamestate")
        assertEquals(redoState.state, currentState.state)
        assertEquals(redoState.currentPlayer, currentState.currentPlayer)
        assertEquals(0, game.redoStack.size)
    }

    /**
     * Tests if an exception is thrown when trying to redo a turn without having undone a turn beforehand
     */
    @Test
    fun redoWithNoTurnUndone(){
        rootService.currentGame = AzulGame(redoWorks)
        val game = rootService.currentGame
        checkNotNull(game)

        assertThrows<IllegalStateException> { playerActionService.redo() }
    }
}