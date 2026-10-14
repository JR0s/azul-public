package gui

import gui.VisualMode
import gui.scenes.subComponants.Game
import gui.scenes.MenuScene
import gui.scenes.OfflineMainMenuScene
import gui.scenes.OnlineMainMenuScene
import gui.scenes.GameOverScene
import gui.scenes.OfflineGameScene
import gui.scenes.OnlineGameScene
import gui.scenes.SelectGameModeScene
import service.Refreshable
import tools.aqua.bgw.core.BoardGameApplication
import service.RootService
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.visual.ImageVisual

/**
 * Represents the main application for the SoPra board game.
 * The application initializes the [RootService] and displays the scenes.
 */
object AzulApplication : BoardGameApplication("SoPra Game"), Refreshable {

    /**
     * The root service instance. This is used to call service methods and access the entity layer.
     */
    val rootService: RootService = RootService()
    var mode = VisualMode.NORMAL
    private val gameOverScene: GameOverScene = GameOverScene(rootService)

    private val menuScene: MenuScene = MenuScene(rootService).apply {
        resumeButton.onMouseClicked={
            hideMenuScene()
        }
    }
    /**
     * The main game scene displayed in the application.
     */
    val Onliegame = OnlineGameScene(rootService)
    val Offliegame = OfflineGameScene(rootService)


    private val onlineMainMenuScene = OnlineMainMenuScene(rootService)
    private val offlineMainMenuScene = OfflineMainMenuScene(rootService)
    private val selectGameModeScene = SelectGameModeScene(rootService).apply {
        onlineButton.onMouseClicked = {
            hideMenuScene()
            showMenuScene(onlineMainMenuScene)
        }
        offlineButton.onMouseClicked = {
            hideMenuScene()
            showMenuScene(offlineMainMenuScene)
        }
    }

    /**
     * Initializes the application by displaying the [Game].
     */
    init {


        this.showGameScene(BoardGameScene(1920,1080).apply {
            background = gui.scenes.builder.VisKey.getVisual(gui.scenes.builder.VisKey.BACKGROUND, AzulApplication.mode)
        })
        this.showMenuScene(selectGameModeScene)
        onlineMainMenuScene.backButton.onMouseClicked = {
                hideMenuScene()
                showMenuScene(selectGameModeScene)
                rootService.removeRefreshable(Onliegame)
        }
        offlineMainMenuScene.backButton.onMouseClicked = {
            hideMenuScene()
            showMenuScene(selectGameModeScene)
            rootService.removeRefreshable(Offliegame)
        }
        offlineMainMenuScene.loadButton.onMouseClicked = {
            hideMenuScene()
            rootService.gameService.loadGame("Savegame.json")
        }
        gameOverScene.restartButton.onMouseClicked = {
            hideMenuScene()
            showMenuScene(selectGameModeScene)
        }
        Offliegame.game.menuButton.onMouseClicked= {
            showMenuScene(menuScene)
        }

        rootService.addRefreshables(
            this,
            gameOverScene,
            menuScene,
            onlineMainMenuScene,
            offlineMainMenuScene,
            Onliegame,
            selectGameModeScene)
    }

    override fun refreshAfterGameOver() {
        showMenuScene(gameOverScene)
    }
}
