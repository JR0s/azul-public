package gui.scenes.builder

import gui.VisualMode
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual

enum class VisKey {
    AZUL,
    BACKGROUND,
    BLACK,
    BLUE,
    BOARD_SHORT,
    BOARD,
    BOT,
    CARD_DECK,
    FACTORY_BLACK,
    FACTORY_WHITE,
    FACTORYS,
    HUMAN,
    KRONE,
    ONLINE,
    ORANGE,
    PLAYER_TOKEN,
    RED,
    WHITE,
    EMPTY;


    companion object {
        private val normalMap = mapOf(
            AZUL to "normal/Azul.png",
            BACKGROUND to "normal/Background.png",
            BLACK to "normal/black.png",
            BLUE to "normal/blue.png",
            BOARD_SHORT to "normal/Board-short.png",
            BOARD to "normal/Board.png",
            BOT to "normal/Bot.png",
            CARD_DECK to "normal/card_deck.png",
            FACTORY_BLACK to "normal/Factory-black.png",
            FACTORY_WHITE to "normal/Factory-white.png",
            FACTORYS to "normal/Factorys.png",
            HUMAN to "normal/Human.png",
            KRONE to "normal/Krone.png",
            ONLINE to "normal/Online.png",
            ORANGE to "normal/orange.png",
            PLAYER_TOKEN to "normal/player-token.png",
            RED to "normal/red.png",
            WHITE to "normal/white.png",
            EMPTY to ""
        )
        private val rottenMap = mapOf(
            AZUL to "normal/Azul.png",
            BACKGROUND to "brainRot/bg.gif",
            BLACK to "brainRot/pedro.gif",
            BLUE to "brainRot/happy.gif",
            BOARD_SHORT to "normal/Board-short.png",
            BOARD to "rotten/Board.png",
            BOT to "brainRot/bot.gif",
            CARD_DECK to null,
            FACTORY_BLACK to null,
            FACTORY_WHITE to "brainRot/fish.gif",
            FACTORYS to null,
            HUMAN to "brainRot/megalul.gif",
            KRONE to null,
            ONLINE to "brainRot/online.gif",
            ORANGE to "brainRot/yello.gif",
            PLAYER_TOKEN to "brainRot/start.gif",
            RED to "brainRot/commi.gif",
            WHITE to "brainRot/white.gif",
            EMPTY to ""
        )

        fun getVisual(key: VisKey, mode: VisualMode): Visual {
            val path = when (mode) {
                VisualMode.NORMAL -> normalMap[key]
                VisualMode.ROTTEN -> rottenMap[key]
            }

            if(path == null && mode == VisualMode.ROTTEN) {
                return getVisual(key, VisualMode.NORMAL)
            }

            if (path == ""|| path == null) return Visual.EMPTY
            return   ImageVisual(path)
        }
    }
}