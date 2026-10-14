package service
 import entity.*
/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the GUI classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * GUI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {

    /**
     * refresh to fill the factories
     */
    fun refreshAfterStartNewRound() {}

    /**
     * refresh to display new current player
     */
    fun refreshAfterNewTurn() {}

    /**
     * refresh to play animation for selection from factory
     */
    fun refreshAfterTakeTilesFromFactory(color: Color, factoryIndex: Int, takenCount: Int) {}

    /**
     * perform refreshes that are necessary after a game is over
     */
    fun refreshAfterGameOver() {}

    /**
     * refresh to update the view after an undo
     */
    fun refreshAfterUndo() {}

    /**
     * refresh to update the view after an undo
     */
    fun refreshAfterRedo() {}

    /**
     * refresh to update the viw after an undo
     */
    fun refreshAfterEndTurn() {}

    /**
     * refresh to update the view after loading a game
     */
    fun refreshAfterLoadGame() {}

    /**
     * perform refreshes that are necessary after the round is over
     */
    fun refreshAfterEndRound(currentPlayer: Int) {}

    /**
     * refresh to display a confirmation of saved
     */
    fun refreshAfterSaveGame() {}

    /**
     * refresh to create a new game view with a specific amount of players
     */
    fun refreshAfterStartGame(amountPlayer: Int){}

    /**
     * refresh to play animation for selection from middle
     */
    fun refreshAfterTakeTilesFromMiddle(color: Color){}

    /**
     * refresh to play animation for place in patternLine
     */
    fun refreshAfterPlaceTilesInPatternLine(playerNr: Int, line: Int, amount: Int){}

    /**
     * refresh to play animation for place in floorline
     */
    fun refreshAfterPlaceTilesInFloorLine(playerNr: Int, amount: Int){}

    /**
     * refresh the NameList for online game
     */
    fun refreshAfterNetworkPlayerJoined( playerNames : List<String>){}
    /**
     * refresh the speed after it was changed
     */
    fun refreshAfterSpeedSet(speed: Int){}


    /**
     * refresh the visual mode after it was changed
     */
    fun refreshAfterVisualselection(){}
}