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
 * Unit tests for undoing a turn in Azul Game.
 */
class UndoTest {
    private lateinit var rootService: RootService
    private lateinit var playerActionService: PlayerActionService

    val board1: Board = Board(
        arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})
    val board2: Board = Board(
        arrayOf(PatternLine(1)), MutableList(7){Color.BLACK})

    val player1 = Player("Neo", 0, board1, PlayerType.HUMAN)
    val player2 = Player("Rohan", 0, board2, PlayerType.HUMAN)

    val undoWorks = AzulGameState(
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
     * Tests that an exception is thrown when trying to undo a turn with no current game active
     */
    @Test
    fun undoNoGame(){
        rootService.currentGame = null
        assertThrows<IllegalStateException>{ playerActionService.undo() }
    }

    /**
     * Tests if the turn is correctly undone by "simulating" a turn and checking
     * if the stacks are correctly adjusted
     */
    @Test
    fun undoWorks(){
        rootService.currentGame = AzulGame(undoWorks)
        val game = rootService.currentGame
        checkNotNull(game)
        val gamestate = game.azulGameState

        gamestate.currentPlayer = 1
        gamestate.state = GameState.END_TURN
        game.undoStack.push(gamestate)
        assertDoesNotThrow{playerActionService.undo()}

        val undoneState = rootService.currentGame?.azulGameState ?: error("No active gamestate")
        assertEquals(undoWorks.state, undoneState.state)
        assertEquals(undoWorks.currentPlayer, undoneState.currentPlayer)
        assertEquals(1, game.redoStack.size)
    }

    /**
     * Tests if an exception is thrown when trying to undo a turn without having done a turn beforehand
     */
    @Test
    fun undoWithNoTurnPlayed(){
        rootService.currentGame = AzulGame(undoWorks)
        val game = rootService.currentGame
        checkNotNull(game)

        assertThrows<IllegalStateException> { playerActionService.undo() }
    }
}