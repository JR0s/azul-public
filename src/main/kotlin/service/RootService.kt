package service

import entity.AzulGame
import service.network.NetworkService

/**
 * The root service class is responsible for managing services and the entity layer reference.
 * This class acts as a central hub for every other service within the application.
 */
class RootService {

    /**
     * Holds the current game instance or null if no game is running.
     */
    var currentGame: AzulGame? = null

    // Services that use this root service
    val gameService = GameService(this)

    val playerActionService = PlayerActionService(this)

    val networkService = NetworkService(this)
    /**
     * Adds the provided [newRefreshable] to all services connected
     * to this root service
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * Removes the provided [refreshable] from all services connected
     * to this root service
     */
    fun removeRefreshable(refreshable: Refreshable) {
        gameService.removeRefreshable(refreshable)
        playerActionService.removeRefreshable(refreshable)
        networkService.removeRefreshable(refreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }
}
