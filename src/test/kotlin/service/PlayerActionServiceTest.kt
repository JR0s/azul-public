package service

import entity.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Unit-Tests für den [PlayerActionService].
 *
 * - Nutzt einen kleinen Test-Harness ([TestContext]) zur Initialisierung von RootService und Game-State.
 * - Registriert pro Test ein anonymes [Refreshable], um die an die GUI gemeldeten Parameter mitzuschneiden.
 * - **Wichtig:** `Board.floorLine` ist ein `Array<Color>`; freie Plätze sind `Color.EMPTY`.
 */
class PlayerActionServiceTest {

    /**
     * Test throws an error when no game is active
     */
    @Test
    fun takeTilesFromMiddleNoGame() {
        val ctx = TestContext()
        ctx.root.currentGame = null
        val ex = assertThrows<IllegalStateException> {
            ctx.service.takeTilesFromMiddle(Color.YELLOW)
        }
        assertEquals("No active game", ex.message)
    }

    /**
     * Test throws an error with wrong player index
     */
    @Test
    fun takeTilesFromMiddleWrongPlayer() {
        val ctx = TestContext()
        ctx.state.currentPlayer = 6
        val ex = assertThrows<IllegalArgumentException> {
            ctx.service.takeTilesFromMiddle(Color.YELLOW)
        }
        assertEquals("It's not currently any player's turn", ex.message)
    }

    /**
     * Test throws an error with wrong state
     */
    @Test
    fun takeTilesFromMiddleWrongState() {
        val ctx = TestContext()
        ctx.state.state = GameState.PLACE
        val ex = assertThrows<IllegalStateException> {
            ctx.service.takeTilesFromMiddle(Color.YELLOW)
        }
        assertEquals("Game state PLACE must be TAKE to take tiles from middle", ex.message)
    }

    /**
     * Test throws an error with trying to take start marker
     */
    @Test
    fun takeTilesFromMiddleStartMarker() {
        val ctx = TestContext()
        val ex = assertThrows<IllegalArgumentException> {
            ctx.service.takeTilesFromMiddle(Color.START_MARKER)
        }
        assertEquals("You cannot choose START_MARKER as a color to take", ex.message)
    }

    /**
     * Test throws an error with trying to take start marker
     */
    @Test
    fun takeTilesFromMiddleWrongColor() {
        val ctx = TestContext()
        ctx.state.middle.tiles = mutableListOf(Color.START_MARKER, Color.BLUE, Color.BLUE, Color.RED)
        val ex = assertThrows<IllegalArgumentException> {
            ctx.service.takeTilesFromMiddle(Color.YELLOW)
        }
        assertEquals("The middle doesn't contain tiles of the specified color", ex.message)
    }

    /**
     * Throws when currentPlayer index is invalid.
     */
    @Test
    fun placeTilesOntoBoard_invalidCurrentPlayer() {
        val ctx = TestContext()
        val gs = ctx.state
        gs.state = GameState.PLACE
        gs.currentPlayer = 5  // out of range!
        gs.takenTiles = Color.BLUE to 2

        val ex = assertThrows<IllegalArgumentException> {
            ctx.service.placeTilesOntoBoard(0, 0)
        }
        assertEquals("It's not currently any player's turn", ex.message)
    }

    /**
     * Throws when game state is not PLACE.
     */
    @Test
    fun placeTilesOntoBoard_wrongGameState() {
        val ctx = TestContext()
        val gs = ctx.state
        gs.currentPlayer = 0
        gs.state = GameState.TAKE // Wrong state
        gs.takenTiles = Color.RED to 2

        val ex = assertThrows<IllegalStateException> {
            ctx.service.placeTilesOntoBoard(gs.currentPlayer, 0)
        }
        assertEquals("Game state must be PLACE to place tiles on board", ex.message)
    }

    /**
     * Throws when no tiles have been taken.
     */
    @Test
    fun placeTilesOntoBoard_noTilesTaken() {
        val ctx = TestContext()
        val gs = ctx.state
        gs.currentPlayer = 0
        gs.state = GameState.PLACE
        gs.takenTiles = Color.INVISIBLE to 0 // Nothing taken

        val ex = assertThrows<IllegalArgumentException> {
            ctx.service.placeTilesOntoBoard(gs.currentPlayer, 0)
        }
        assertEquals("No tiles have been taken to place", ex.message)
    }

    /**
     * Throws when placing tiles of a different color into a filled pattern line.
     */
    @Test
    fun placeTilesOntoBoard_conflictingPatternLineColor() {
        val ctx = TestContext()
        val gs = ctx.state
        val player = gs.players[0]
        gs.currentPlayer = 0
        gs.state = GameState.PLACE
        gs.takenTiles = Color.RED to 2

        // Set up pattern line with different color BLUE already filled partially
        val patternLine = player.board.patternLines[0]
        patternLine.color = Color.BLUE
        patternLine.fill = 1

        val ex = assertThrows<IllegalStateException> {
            ctx.service.placeTilesOntoBoard(gs.currentPlayer, 0)
        }
        assertEquals("Pattern line contains tiles of a different color", ex.message)
    }

    /**
     * Throws when the wall row already has this color.
     */
    @Test
    fun placeTilesOntoBoard_colorAlreadyOnWall() {
        val ctx = TestContext()
        val gs = ctx.state
        val player = gs.players[0]
        gs.currentPlayer = 0
        gs.state = GameState.PLACE
        gs.takenTiles = Color.BLUE to 1

        // Fill the first wall row with a BLUE tile
        val wallRow = player.board.wall[0]
        wallRow[1].filled = true
        wallRow[1].color = Color.BLUE

        val ex = assertThrows<IllegalArgumentException> {
            ctx.service.placeTilesOntoBoard(gs.currentPlayer, 0)
        }
        assertEquals("The corresponding wall row already has a tile of this color", ex.message)
    }

    /**
     * Testet den Basisfluss von `takeTilesFromFactory`.
     */
    @Test
    fun takeTilesFromFactory_basicFlow() {
        val ctx = TestContext()
        val gs = ctx.state
        gs.state = GameState.TAKE
        gs.currentPlayer = 0

        // Factory[0] = BLUE, RED, BLUE
        val f0 = gs.factories[0]
        f0.tiles = mutableListOf(Color.BLUE, Color.RED, Color.BLUE)
        gs.middle.tiles = mutableListOf()

        // Recorder inline
        var recCalled = false
        var recColor: Color? = null
        var recFactoryIndex: Int? = null
        var recTakenCount: Int? = null
        ctx.root.addRefreshable(object : Refreshable {
            override fun refreshAfterTakeTilesFromFactory(color: Color, factoryIndex: Int, takenCount: Int) {
                recCalled = true
                recColor = color
                recFactoryIndex = factoryIndex
                recTakenCount = takenCount
            }
        })

        // Act
        ctx.service.takeTilesFromFactory(recFactoryIndex ?: 0, Color.BLUE)

        // Assert: Zustand
        assertEquals(Color.BLUE, gs.takenTiles.first)
        assertEquals(2, gs.takenTiles.second)
        assertEquals(f0.tiles[0], Color.INVISIBLE, "Factory should be emptied")
        assertEquals(listOf(Color.RED), gs.middle.tiles, "Leftover RED goes to middle")
        assertEquals(GameState.PLACE, gs.state)

        // Assert: Callback
        assertTrue(recCalled)
        assertEquals(Color.BLUE, recColor)
        assertEquals(0, recFactoryIndex)  // f0 ist factories[0]
        assertEquals(2, recTakenCount)

        ctx.root.removeRefreshable(object : Refreshable {
            override fun refreshAfterTakeTilesFromFactory(color: Color, factoryIndex: Int, takenCount: Int) {
                recCalled = true
                recColor = color
                recFactoryIndex = factoryIndex
                recTakenCount = takenCount
            }
        })

        ctx.root.addRefreshables(object : Refreshable {
            override fun refreshAfterTakeTilesFromFactory(color: Color, factoryIndex: Int, takenCount: Int) {
                recCalled = true
                recColor = color
                recFactoryIndex = factoryIndex
                recTakenCount = takenCount
            }
        })
    }

    /**
     * Testet `placeTilesOntoBoard`.
     */
    @Test
    fun placeTilesOntoBoard_placesUpToCapacity_andClearsTaken() {
        val ctx = TestContext()
        val gs = ctx.state
        val player = gs.players[0]
        gs.currentPlayer = 0
        gs.state = GameState.PLACE
        // factories dürfen nicht leer, sonst end round
        gs.factories= mutableListOf(Factory(mutableListOf(Color.BLUE,Color.RED)))

        // 4 BLUE; Pattern-Line Größe 3 (Index 2)
        gs.takenTiles = Color.BLUE to 4
        val pl = player.board.patternLines[2]
        assertEquals(3, pl.size)

        ctx.root.addRefreshable(object : Refreshable {
        })

        // Act
        ctx.service.placeTilesOntoBoard(gs.currentPlayer, 2)

        // Assert: Zustand
        assertEquals(3, pl.fill, "Should place up to capacity (3)")
        assertEquals(Color.BLUE, pl.color, "Line color should be set on first placement")
        assertEquals(gs.takenTiles.first, Color.INVISIBLE)
        assertEquals(0, gs.takenTiles.second)
    }

    /**
     * Testet `takeTilesFromMiddle` beim ersten Griff aus der Mitte (Startmarker).
     */
    @Test
    fun takeTilesFromMiddle_firstTaker_placesMarkerOnFloor_andSetsPlace() {
        val ctx = TestContext()
        val gs = ctx.state
        gs.state = GameState.TAKE
        gs.currentPlayer = 0

        // Mitte = [START_MARKER, BLUE, BLUE, RED]
        gs.middle.tiles = mutableListOf(Color.START_MARKER, Color.BLUE, Color.BLUE, Color.RED)

        // Recorder inline
        var recCalled = false
        var recColor: Color? = null
        ctx.root.addRefreshable(object : Refreshable {
            override fun refreshAfterTakeTilesFromMiddle(color: Color) {
                recCalled = true
                recColor = color
            }
        })

        // Act
        ctx.service.takeTilesFromMiddle(Color.BLUE)

        // Assert: Zustand
        assertEquals(Color.BLUE, gs.takenTiles.first)
        assertEquals(2, gs.takenTiles.second)
        assertEquals(GameState.PLACE, gs.state)
        assertFalse(gs.middle.tiles.contains(Color.START_MARKER))
        assertEquals(listOf(Color.RED), gs.middle.tiles)

        // FloorLine ist Array<Color> mit Color.EMPTY -> Marker muss in einem Slot liegen
        val floor = gs.players[0].board.floorLine
        val idx = floor.indexOfFirst { it == Color.START_MARKER }
        assertTrue(idx >= 0, "Marker should be placed on the first available floor slot")

        // Assert: Callback
        assertTrue(recCalled)
        assertEquals(Color.BLUE, recColor)
    }

    //-- helpers --

    /**
     * Mini-Harness: baut [RootService], [AzulGame] und einen frischen [AzulGameState] auf.
     * Floor-Line wird als `Array<Color>` mit `Color.EMPTY` initialisiert.
     */
    private class TestContext {
        /** Gemeinsamer RootService. */
        val root = RootService()
        /** Der aktuelle Spielzustand. */
        val state: AzulGameState
        /** Zu testender Service. */
        val service: PlayerActionService = root.playerActionService

        init {
            val p1 = Player("P1", 0, newBoard(), PlayerType.HUMAN)
            val p2 = Player("P2", 0, newBoard(), PlayerType.HUMAN)

            state = AzulGameState(
                currentPlayer = 0,
                state = GameState.TAKE,
                players = mutableListOf(p1, p2)
            )

            state.factories.addAll(listOf(Factory(mutableListOf()), Factory(mutableListOf())))
            state.middle.tiles = mutableListOf()
            state.takenTiles = Color.INVISIBLE to 0
            state.usedTiles = mutableListOf()

            val game = AzulGame(state)
            root.currentGame = game
        }

        /**
         * Board:
         * - Pattern-Lines (1..5),
         * - Wall leer,
         * - Floor-Line mit `Color.EMPTY` als freier Slot.
         */
        private fun newBoard(): Board {
            val patternLines = Array(5) { i -> PatternLine(i + 1) }

            return Board(patternLines )
        }
    }
}
