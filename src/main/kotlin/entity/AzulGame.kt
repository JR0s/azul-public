package entity

import gui.VisualMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Stack

@Serializable
/**
 * Root class that has all past and current game states.
 *
 * @property azulGameState Current game state in [AzulGameState].
 * @property undoStack Stack holding previous game states to allow undoing turns.
 * @property redoStack Stack holding undone game states to allow redoing turns.
 */
class AzulGame(
    var azulGameState: AzulGameState,
    var speed: Int = 5000,
    var visualMode: VisualMode = VisualMode.NORMAL
) {
    @Contextual val undoStack: Stack<AzulGameState> = Stack()
    @Contextual val redoStack: Stack<AzulGameState> = Stack()
}