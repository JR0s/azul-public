package gui.scenes

import gui.VisualMode
import gui.AzulApplication
import gui.Sounds
import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.RadioButton
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * MenuScene is the scene shown when the game is paused.
 */
class MenuScene(val rootService: RootService) : MenuScene(400, 250), Refreshable {

    val resumeButton = Button(
        width = 200, height = 50,
        posX = 100, posY = 25,
        text = "Resume"
    ).apply {
        visual=ColorVisual(128, 128, 128)
    }

    val quitButton = Button(
        width = 200, height = 50,
        posX = 100, posY = 175,
        text = "Quit"
    ).apply {
        visual=ColorVisual(128, 128, 128)
        onMouseClicked={
            AzulApplication.exit()
        }
    }

    val saved = Label(80,65,240,60,"Game was saved",
        visual=ColorVisual(128, 128, 128,0.4), font = Font(size = 30))

    val saveButton = Button(
        width = 200, height = 50,
        posX = 100, posY = 100,
        text = "Save"
    ).apply {
        visual=ColorVisual(128, 128, 128)
        onMouseClicked={
            rootService.gameService.saveGame("Savegame.json")
            if(AzulApplication.mode == VisualMode.ROTTEN)
                Sounds().playSound(Sounds.SoundKey.ONSAVE)
        }

    }



    init {
        addComponents(resumeButton,quitButton,saveButton,saved)
        background = ColorVisual(0, 0, 0, 0)
        saved.isVisible=false
    }

    /**
     * add animation to refreshAfterSaveGame
     */
    override fun refreshAfterSaveGame() {
        saved.isVisible=true
        playAnimation(DelayAnimation(600).apply { onFinished={
            saved.isVisible = false
        }})
    }
}