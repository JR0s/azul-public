package gui.scenes

import gui.VisualMode
import gui.AzulApplication
import service.AbstractRefreshingService
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual

/**
 * SelectGameModeScene is the scene shown when the game is started.
 * It allows the user to choose between online and offline mode.
 */

class SelectGameModeScene(val rootService: RootService) : MenuScene(600, 800), Refreshable  {

    private val logo : Label = Label(100, 50, 400, 300).apply { onMouseClicked={
        AzulApplication.mode = VisualMode.ROTTEN
    } }

    val onlineButton = Button(
        width = 200, height = 80,
        posX = 200, posY = 530,
        text = "Online",
        font = Font(size = 22)
    ).apply {
        visual = ColorVisual(221, 136, 136)

    }

    val quitButton = Button(
        width = 140, height = 35,
        posX = 230, posY = 700,
        text = "Quit"
    ).apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked = {
            AzulApplication.exit()
        }
    }

    val offlineButton = Button(
        width = 200, height = 80,
        posX = 200, posY = 440,
        text = "Offline",
        font = Font(size = 22)
    ).apply {
        visual = ColorVisual(136, 221, 136)
    }


    /**
     * initialize the components
     */
    init {
        addComponents(
            quitButton,offlineButton,onlineButton, logo,
        )
        logo.visual=ImageVisual("normal/Azul.png")
        background = ColorVisual(0, 0, 0, 0)



    }
}