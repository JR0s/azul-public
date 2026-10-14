package entity

/**
 * Enum to show current game state to allow/block certain actions in game.
 */
enum class GameState {
    NEW_GAME,
    START_TURN,
    END_TURN,
    TAKE,
    PLACE,
    START_NEW_ROUND,
    END_ROUND,
    GAME_OVER;
}