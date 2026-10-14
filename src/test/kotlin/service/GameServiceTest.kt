package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.*

/**
 * Testklasse für [GameService], die grundlegende Funktionalitäten
 * wie Spielstart, Speichern und Laden von Spielständen überprüft.
 *
 * Verwendet [TestRefreshable], um sicherzustellen, dass die entsprechenden
 * GUI-Refresh-Methoden nach Aktionen aufgerufen werden.
 */
class GameServiceTest {

    private lateinit var rootService: RootService
    private lateinit var gameService: GameService
    private lateinit var testRefreshable: TestRefreshable
    private val testFile = "azul_test_save.json"

    /**
     * Initialisiert den [RootService], den [GameService] und
     * hängt ein [TestRefreshable] an, bevor jeder Testfall ausgeführt wird.
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = GameService(rootService)

        testRefreshable = TestRefreshable()
        gameService.addRefreshable(testRefreshable)
    }

    /**
     * Überprüft, ob ein neues Spiel korrekt initialisiert wird:
     * - Spieleranzahl stimmt
     * - Steine befinden sich in Beutel und Fabriken
     * - Startmarker ist im Zentrum vorhanden
     * - Refreshable-Methode [TestRefreshable.refreshAfterStartGame] wird aufgerufen
     */
    @Test
    fun testStartNewGame_initializesCorrectly() {
        gameService.startNewGame(
            online = false,
            players = arrayOf("Alice" to PlayerType.HUMAN, "Bob" to PlayerType.HUMAN)
        )

        val game = rootService.currentGame
        assertNotNull(game, message = "Game should be initialized")
        val gameState = game.azulGameState

        assertEquals(2, gameState.players.size, message = "There should be 2 players")

        val expectedTiles = (Color.entries.size - 2) * 20
        val actualTiles = gameState.bag.size + gameState.factories.sumOf { it.tiles.size }
        assertEquals(expectedTiles, actualTiles, message = "All tiles must be distributed in bag and factories")

        assertEquals(listOf(Color.START_MARKER), gameState.middle.tiles,
            message = "Middle must contain start marker")

        assertTrue(testRefreshable.started, "refreshAfterStartGame should be called")
    }

    /**
     * Testet, ob das Speichern eines Spiels funktioniert:
     * - JSON-Datei wird erzeugt
     * - Datei enthält nicht-leeren Inhalt
     * - Refreshable-Methode [TestRefreshable.refreshAfterSaveGame] wird aufgerufen
     */
    @Test
    fun testSaveGame_createsJsonFile() {
        gameService.startNewGame(
            false,
            arrayOf(
                "Alice" to PlayerType.HUMAN,
                "Bob" to PlayerType.HUMAN
            )
        )
        gameService.saveGame(testFile)

        val file = File(testFile)
        assertTrue(file.exists(), "Save file should exist")
        assertTrue(file.readText().isNotBlank(), "Save file should contain JSON")
        assertTrue(testRefreshable.saved, "refreshAfterSaveGame should be called")

        file.delete()
    }

    /**
     * Testet, ob ein zuvor gespeichertes Spiel korrekt geladen wird:
     * - Spiel wird initialisiert und gespeichert
     * - Anschließend erneut geladen
     * - Spieleranzahl bleibt identisch
     * - Refreshable-Methode [TestRefreshable.refreshAfterLoadGame] wird aufgerufen
     */
    @Test
    fun testLoadGame_restoresGame() {
        gameService.startNewGame(false, arrayOf("Alice" to PlayerType.HUMAN,
            "Bob" to PlayerType.HUMAN))
        gameService.saveGame(testFile)
        val original = rootService.currentGame
        assertNotNull(original, message = "Game must be saved first")

        gameService.loadGame(testFile)
        val loaded = rootService.currentGame
        assertNotNull(loaded, message = "Game must be loaded")

        assertEquals(
            original.azulGameState.players.size,
            loaded.azulGameState.players.size,
            message = "Player count should match"
        )
        assertTrue(testRefreshable.loaded, "refreshAfterLoadGame should be called")

        File(testFile).delete()
    }

    /**
     * Stellt sicher, dass ein Fehler geworfen wird,
     * wenn versucht wird ein Spiel zu speichern,
     * obwohl kein aktuelles Spiel vorhanden ist.
     */
    @Test
    fun testSaveGameFailsWhenNoGame() {
        rootService.currentGame = null
        val exception = assertFailsWith<IllegalStateException> {
            gameService.saveGame(testFile)
        }
        assertEquals("Kein Spiel zum Speichern vorhanden", exception.message)
    }

    /**
     * Stellt sicher, dass ein Fehler geworfen wird,
     * wenn versucht wird, ein Spiel aus einer nicht existierenden Datei zu laden.
     */
    @Test
    fun testLoadGameFailsOnInvalidFile() {
        val exception = assertFailsWith<IllegalArgumentException> {
            gameService.loadGame("does_not_exist.json")
        }
        assertTrue(exception.message!!.contains("existiert nicht"))
    }

    /**
     * Checks if correct amount of factories are initialized in game start
     */
    @Test
    fun testStartNewGame_factoriesCount() {
        gameService.startNewGame(
            online = false,
            players = arrayOf("Alice" to PlayerType.HUMAN, "Bob" to PlayerType.HUMAN, "Kevin" to PlayerType.HUMAN)
        )

        //7 factories for 3 players
        var game = rootService.currentGame
        var gameState = game?.azulGameState ?: error("No game")
        assertEquals(7, gameState.factories.size)

        rootService.currentGame = null
        gameService.startNewGame(
            online = false,
            players = arrayOf("Alice" to PlayerType.HUMAN, "Bob" to PlayerType.HUMAN, "Kevin" to PlayerType.HUMAN,
                "Allison" to PlayerType.HUMAN)
        )

        //9 factories for 4 players
        game = rootService.currentGame
        gameState = game?.azulGameState ?: error("No game")
        assertEquals(9, gameState.factories.size)

        //error: too many players
        rootService.currentGame = null
        var ex = assertThrows<IllegalArgumentException> {
            gameService.startNewGame(
                online = false,
                players = arrayOf("Alice" to PlayerType.HUMAN, "Bob" to PlayerType.HUMAN, "Kevin" to PlayerType.HUMAN,
                    "Allison" to PlayerType.HUMAN, "Ken" to PlayerType.HUMAN)
            )
        }
        assertEquals("Ungültige Spieleranzahl: 5", ex.message)

        //error: no players
        rootService.currentGame = null
        ex = assertThrows<IllegalArgumentException> {
            gameService.startNewGame(
                online = false,
                players = arrayOf()
            )
        }
        assertEquals("Es muss mindestens einen Spieler geben", ex.message)
    }
}
