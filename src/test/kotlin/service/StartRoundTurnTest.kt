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
 * Unit tests for starting Round and Turn in Azul Game.
 */
class StartRoundTurnTest {
    private lateinit var rootService: RootService
    private lateinit var gameService: GameService

    val mockPlayer1 = Player("Bob", 0, Board(arrayOf(PatternLine(1)),
        mutableListOf()), PlayerType.HUMAN)
    val mockPlayer2 = Player("Tim", 0, Board(arrayOf(PatternLine(1)),
        mutableListOf()), PlayerType.HUMAN)
    val factoriesList = mutableListOf(Factory(), Factory())
    val bagList = mutableListOf(Color.BLUE, Color.BLUE, Color.BLUE, Color.YELLOW,
            Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW)
    val usedtilesList = mutableListOf(Color.YELLOW, Color.YELLOW, Color.YELLOW)

    val mockGameWrongState = AzulGameState(1, GameState.GAME_OVER,
        mutableListOf(mockPlayer1, mockPlayer2))
    val mockGameStartTurn = AzulGameState(1, GameState.START_TURN,
        mutableListOf(mockPlayer1, mockPlayer2))
    val mockGameStartRound = AzulGameState(0, GameState.START_NEW_ROUND,
        mutableListOf(mockPlayer1, mockPlayer2)).apply {
        factories = factoriesList
        bag = bagList
        usedTiles = usedtilesList
    }

    /**
     * set up before each test
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = GameService(rootService)

    }

    /** Tests that starting a round without a game throws an exception. */
    @Test
    fun startNewRoundNoGame(){
        rootService.currentGame = null
        val ex = assertThrows<IllegalStateException> {
            gameService.startNewRound()
        }
        assertEquals("No active game", ex.message)
    }

    /** Tests that starting a turn without a game throws an exception. */
    @Test
    fun startNewTurnNoGame(){
        rootService.currentGame = null
        val ex = assertThrows<IllegalStateException> {
            gameService.startNewTurn()
        }
        assertEquals("No active game", ex.message)
    }

    /** Tests that starting a round in wrong game state throws an exception. */
    @Test
    fun startNewRoundWrongState(){
        rootService.currentGame = AzulGame(mockGameWrongState)
        val ex = assertThrows<IllegalStateException> {
            gameService.startNewRound()
        }
        assertEquals("Game state must be START_NEW_ROUND", ex.message)
    }

    /** Tests that starting a turn in wrong game state throws an exception. */
    @Test
    fun startNewTurnWrongState(){
        rootService.currentGame = AzulGame(mockGameWrongState)
        val ex = assertThrows<IllegalStateException> {
            gameService.startNewTurn()
        }
        assertEquals("Game state must be START_TURN", ex.message)
    }

    /** Tests starting new turn. */
    @Test
    fun startNewTurnNoError(){
        rootService.currentGame = AzulGame(mockGameStartTurn)
        gameService.startNewTurn()
        assertEquals(GameState.TAKE, rootService.currentGame?.azulGameState?.state)

    }

    /** Tests starting new round. */
    @Test
    fun startNewRoundNoError(){
        rootService.currentGame = AzulGame(mockGameStartRound)
        gameService.startNewRound()
        val factories = rootService.currentGame?.azulGameState?.factories ?: error("No factories")

        // Check contents of factory 0
        assertEquals(listOf(Color.BLUE, Color.BLUE, Color.BLUE, Color.YELLOW), factories[0].tiles)

        // Check contents of factory 1
        assertEquals(listOf(Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW), factories[1].tiles)

        // Check sizes
        assertEquals(4, factories[0].tiles.size)
        assertEquals(4, factories[1].tiles.size)

        assertEquals(GameState.START_TURN, rootService.currentGame?.azulGameState?.state)

    }
}
