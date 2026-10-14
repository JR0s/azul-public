package service

/**
 * Eine einfache Test-Implementierung von [Refreshable] für das Azul-Spiel.
 *
 * Diese Klasse wird in Unit-Tests verwendet, um zu überprüfen,
 * ob die Methoden des [Refreshable]-Interfaces von [GameService]
 * korrekt aufgerufen werden.
 *
 * Sie speichert nur Zustände in Form von Boolean-Flags und bietet
 * eine [reset]-Methode, um die Flags für nachfolgende Tests zurückzusetzen.
 *
 * @property started true, wenn [refreshAfterStartGame] aufgerufen wurde
 * @property saved true, wenn [refreshAfterSaveGame] aufgerufen wurde
 * @property loaded true, wenn [refreshAfterLoadGame] aufgerufen wurde
 */
class TestRefreshable : Refreshable {

    /** Wird true, wenn [refreshAfterStartGame] aufgerufen wurde. */
    var started = false

    /** Wird true, wenn [refreshAfterSaveGame] aufgerufen wurde. */
    var saved = false

    /** Wird true, wenn [refreshAfterLoadGame] aufgerufen wurde. */
    var loaded = false

    /**
     * Markiert, dass ein neues Spiel gestartet wurde.
     * Setzt [started] auf true.
     */
    override fun refreshAfterStartGame(amountPlayer: Int) {
        started = true
    }

    /**
     * Markiert, dass ein Spiel gespeichert wurde.
     * Setzt [saved] auf true.
     */
    override fun refreshAfterSaveGame() {
        saved = true
    }

    /**
     * Markiert, dass ein Spiel geladen wurde.
     * Setzt [loaded] auf true.
     */
    override fun refreshAfterLoadGame() {
        loaded = true
    }

    /**
     * Setzt alle Flags ([started], [saved], [loaded]) zurück,
     * damit dieselbe Instanz in mehreren Tests verwendet werden kann.
     */
    fun reset() {
        started = false
        saved = false
        loaded = false
    }
}
