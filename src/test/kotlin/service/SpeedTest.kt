package service

import entity.AzulGame
import entity.AzulGameState
import entity.Board
import entity.GameState
import entity.PatternLine
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Unit tests for setting speed in Game.
 */
class SpeedTest {
    private lateinit var rootService: RootService
    private lateinit var playerService: PlayerActionService

    val mockPlayer1 = Player("Bob", 0, Board(arrayOf(PatternLine(1)),
        mutableListOf()), PlayerType.HUMAN)
    val mockPlayer2 = Player("Tim", 0, Board(arrayOf(PatternLine(1)),
        mutableListOf()), PlayerType.HUMAN)
    val mockGame1 = AzulGameState(1, GameState.TAKE,
        mutableListOf(mockPlayer1, mockPlayer2))


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
    fun setSpeedNoGame(){
        rootService.currentGame = null
        val ex = assertThrows<IllegalStateException> {
            playerService.speedSet()
        }
        assertEquals("No active game", ex.message)
    }

    /** Tests with game speed 0. */
    @Test
    fun setSpeedStartWith0(){
        rootService.currentGame = AzulGame(mockGame1, 0)
        playerService.speedSet()
        assertEquals(rootService.currentGame?.speed, 2500)
    }

    /** Tests with game speed not 0. */
    @Test
    fun setSpeedOK(){
        rootService.currentGame = AzulGame(mockGame1, 2500)
        playerService.speedSet()
        assertEquals(rootService.currentGame?.speed, 3000)
    }

    /** Tests with online game start with 0. */
    @Test
    fun setSpeedOnline0(){
        mockGame1.online = true
        rootService.currentGame = AzulGame(mockGame1, 0)
        playerService.speedSet()
        assertEquals(rootService.currentGame?.speed, 500)
    }
}