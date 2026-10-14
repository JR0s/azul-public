package service

import entity.*
import kotlin.test.*

/**
 * This class tests the functionality of endRound, gameOver and score
 */
class EndRoundScoreGameOverTest {
    private lateinit var rootService: RootService
    private lateinit var gameService: GameService

    /**
     * Initialisation before each test
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        gameService = GameService(rootService)
    }

    /**
     * Test the functionality of endRound()
     * Must test implicit whether the scoring is correct
     */
    @Test
    fun testEndRound() {
        val p1 = Player("Alice", 0, newBoard(), PlayerType.HUMAN)
        val p2 = Player("Bob", 0, newBoard(), PlayerType.HUMAN)

        val state = AzulGameState(1, GameState.NEW_GAME, mutableListOf(p1, p2))

        val exceptionNoGame = assertFailsWith<IllegalStateException> { rootService.gameService.endRound() }
        assertEquals("Game has not been started yet.", exceptionNoGame.message)

        rootService.currentGame = AzulGame(state)

        var game = rootService.currentGame?.azulGameState
        assertNotNull(game)

        val exceptionWrongState = assertFailsWith<IllegalArgumentException> { rootService.gameService.endRound() }
        assertEquals("Game state is not END_ROUND but was ${game.state}", exceptionWrongState.message)

        game.state = GameState.END_ROUND

        val exceptionNoMarker = assertFailsWith<IllegalStateException> { rootService.gameService.endRound() }
        assertEquals("No player had the starting marker.", exceptionNoMarker.message)

        game.players[0].board.floorLine.add(Color.START_MARKER)

        // first branch where a new round is started
        rootService.gameService.endRound()
        assertEquals(GameState.START_NEW_ROUND, game.state)


        // prep for 2nd branch
        rootService.currentGame = AzulGame(state)
        game = rootService.currentGame?.azulGameState
        assertNotNull(game)
        game.state = GameState.END_ROUND
        game.players[0].board.floorLine.add(Color.START_MARKER)

        p1.board.wall[0][0].filled = true
        p1.board.wall[0][1].filled = true
        p1.board.wall[0][2].filled = true
        p1.board.wall[0][3].filled = true
        p1.board.wall[0][4].filled = true

        rootService.gameService.endRound()
        assertEquals(GameState.GAME_OVER, game.state)
    }


    /**
     * Test the functionality of gameOver()
     * Must test implicit whether the scoring is correct
     */
    @Test
    fun testGameOver() {
        val p1 = Player("Alice", 0, newBoard(), PlayerType.HUMAN)
        val p2 = Player("Bob", 0, newBoard(), PlayerType.HUMAN)

        val state = AzulGameState(1, GameState.NEW_GAME, mutableListOf(p1, p2))

        val exceptionNoGame = assertFailsWith<IllegalStateException> { rootService.gameService.gameOver() }
        assertEquals("Game has not been started yet.", exceptionNoGame.message)

        rootService.currentGame = AzulGame(state)

        val game = rootService.currentGame?.azulGameState
        assertNotNull(game)

        val exceptionWrongState = assertFailsWith<IllegalArgumentException> { rootService.gameService.gameOver() }
        assertEquals("Game state is not GAME_OVER but was ${game.state}", exceptionWrongState.message)

        game.state = GameState.GAME_OVER

        rootService.gameService.gameOver()

    }

    /**
     * tests whether the scoring works as intended
     */
    @Test
    fun testScoring(){
        val p1 = Player("Alice", 0, newBoard(), PlayerType.HUMAN)
        val p2 = Player("Bob", 0, newBoard(), PlayerType.HUMAN)
        val p3 = Player("Charlie", 0, newBoard(), PlayerType.HUMAN)
        val p4 = Player("Doris", 0, newBoard(), PlayerType.HUMAN)

        // CAREFUL: [y][x] position in grid!!!
        p1.board.wall[0][1].filled = true
        p1.board.wall[1][1].filled = true
        p1.board.wall[2][0].filled = true
        p1.board.wall[2][1].filled = true
        p1.board.wall[2][2].filled = true
        p1.board.wall[2][3].filled = true
        p1.board.wall[2][4].filled = true
        p1.board.wall[3][1].filled = true
        p1.board.wall[3][3].filled = true
        p1.board.wall[4][1].filled = true
        p1.board.wall[4][4].filled = true

        p1.board.patternLines[0].fill = 1
        p1.board.patternLines[0].color = Color.BLUE
        p1.board.floorLine.add(Color.START_MARKER)

        p3.board.wall[0][2].filled = true
        p3.board.wall[1][1].filled = true
        p3.board.wall[1][3].filled = true
        p3.board.wall[2][2].filled = true

        p3.board.patternLines[1].fill = 2
        p3.board.patternLines[1].color = Color.YELLOW
        p3.board.floorLine.add(Color.BLUE)
        p3.board.floorLine.add(Color.BLUE)

        p4.score = 20
        p4.board.floorLine.add(Color.RED)
        p4.board.floorLine.add(Color.RED)
        p4.board.floorLine.add(Color.RED)

        val state = AzulGameState(1, GameState.END_ROUND, mutableListOf(p1, p2, p3, p4))
        rootService.currentGame = AzulGame(state)

        val game = rootService.currentGame?.azulGameState
        assertNotNull(game)

        rootService.gameService.endRound()

        assertEquals(20, game.players[0].score)
        assertEquals(0, game.players[1].score)
        assertEquals(4, game.players[2].score)
        assertEquals(16, game.players[3].score)
    }

    /**
     * tests the edge cases for amount in floorLine
     */
    @Test
    fun testFloorLine(){
        val p1 = Player("Alice", 0, newBoard(), PlayerType.HUMAN)
        val p2 = Player("Bob", 0, newBoard(), PlayerType.HUMAN)
        val p3 = Player("Charlie", 0, newBoard(), PlayerType.HUMAN)
        val p4 = Player("Doris", 0, newBoard(), PlayerType.HUMAN)

        p1.score = 20
        p1.board.floorLine.add(Color.START_MARKER)
        p1.board.floorLine.add(Color.RED)
        p1.board.floorLine.add(Color.RED)
        p1.board.floorLine.add(Color.RED)
        p1.board.floorLine.add(Color.RED)

        p2.score = 20
        p2.board.floorLine.add(Color.RED)
        p2.board.floorLine.add(Color.RED)
        p2.board.floorLine.add(Color.RED)
        p2.board.floorLine.add(Color.RED)
        p2.board.floorLine.add(Color.RED)
        p2.board.floorLine.add(Color.RED)

        p3.score = 20
        p3.board.floorLine.add(Color.RED)
        p3.board.floorLine.add(Color.RED)
        p3.board.floorLine.add(Color.RED)
        p3.board.floorLine.add(Color.RED)
        p3.board.floorLine.add(Color.RED)
        p3.board.floorLine.add(Color.RED)
        p3.board.floorLine.add(Color.RED)

        p4.score = 20
        p4.board.floorLine.add(Color.RED)
        p4.board.floorLine.add(Color.RED)
        p4.board.floorLine.add(Color.RED)
        p4.board.floorLine.add(Color.RED)


        val state = AzulGameState(1, GameState.END_ROUND, mutableListOf(p1, p2, p3, p4))
        rootService.currentGame = AzulGame(state)

        val game = rootService.currentGame?.azulGameState
        assertNotNull(game)

        rootService.gameService.endRound()

        assertEquals(12, game.players[0].score)
        assertEquals(9, game.players[1].score)
        assertEquals(6, game.players[2].score)
        assertEquals(14, game.players[3].score)
    }


    /**
     * helper function to create an empty board
     */
    private fun newBoard(): Board {
        val patternLines = arrayOf(PatternLine(size = 1, fill = 0, color = Color.INVISIBLE),
            PatternLine(size = 2, fill = 0, color = Color.INVISIBLE),
            PatternLine(size = 3, fill = 0, color = Color.INVISIBLE),
            PatternLine(size = 4, fill = 0, color = Color.INVISIBLE),
            PatternLine(size = 5, fill = 0, color = Color.INVISIBLE))

        // IMPORTANT: floorLine must be nullable because the service searches for `null` slots
        val floorLine = mutableListOf<Color>()
        return Board(patternLines, floorLine)
    }
}
