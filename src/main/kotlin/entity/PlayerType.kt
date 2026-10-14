package entity
import kotlinx.serialization.Serializable

@Serializable
/**
 * Enum to save types of players: bot, human(player(s) on current machine),
 * online (online opponent connected via BGW-Net)
 */
enum class PlayerType {
    HUMAN,
    BOT_PRO,
    BOT_RANDOM,
    ONLINE,
    WAIT
}