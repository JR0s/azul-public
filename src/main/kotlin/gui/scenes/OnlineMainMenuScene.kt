package gui.scenes

import entity.PlayerType
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual


/**
 * OnlineMainMenuScene is the scene shown when the game is started in online mode.
 * It allows the user to enter their name and choose to play against bots or join/create a lobby.
 */
class OnlineMainMenuScene(rootService: RootService) : MenuScene(500, 800), Refreshable {

    val backButton = Button(
        width = 140, height = 35,
        posX = 50, posY = 700,
        text = "go back"
    ).apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked={
            rootService.networkService.disconnect()
            showOptions()
        }
    }
    val disconnect = Button(
        width = 140, height = 35,
        posX = 50, posY = 700,
        text = "Disconnect"
    ).apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked={
            rootService.networkService.disconnect()
            showOptions()
        }
    }

    val headLine = Label(
        170, 50, 200, 50, "Enter Name"
    ).apply { font = Font(size = 22)
    }
    var names = mutableListOf<String>()
    val shuffel = CheckBox(75,350,150,35,"Shuffel Players")
    val name : Label = Label(30,125,100,35,"Name:")
    val nameInput: TextField = TextField(width = 200, height = 35, posX = 130, posY = 125, text = "")
    val waiting: Label = Label(50,300,400,50,"Waiting on Host to start the Game").apply { font = Font(25)}
    val isBot: CheckBox = CheckBox(340,120,80,50,"Bot")
    val secret: Label = Label(30,180,100,35,"Secret:")
    val sessionID : Label = Label(30,240,100,35,"Session ID:")
    val secretInput: TextField = TextField(width = 200, height = 35, posX = 130, posY = 180, text = "fliese25")
    val sessionIDInput: TextField = TextField(width = 200, height = 35, posX = 130, posY = 240, text = "")

    val startButton: Button = Button(275,350,150,50,"Start").apply {
        visual = ColorVisual(128, 128, 128)
        if (shuffel.isChecked){
            names.shuffle()
        }
        onMouseClicked={

            rootService.networkService.startNewHostedGame(playerNames = names)

        }
    }
    val players = TextField (400,200,100,100,visual = ColorVisual(0, 0, 0,0))

    // quit buttons einblenden?
    val joinLobbyButton: Button = Button(75,350,150,50,"Join Lobby")
        .apply { visual = ColorVisual(128, 128, 128)
            onMouseClicked={
                rootService.networkService.joinGame(secretInput.text, nameInput.text,
                  if(isBot.isChecked)PlayerType.BOT_PRO else PlayerType.HUMAN,sessionIDInput.text)
                hideOptions()
                waiting.isVisible = true
            }
        }

    val createLobbyButton: Button = Button(275,350,150,50,"Create Lobby")
        .apply { visual = ColorVisual(128, 128, 128)
            onMouseClicked={
                rootService.networkService.hostGame(secretInput.text, nameInput.text,
                  if(isBot.isChecked)PlayerType.BOT_PRO else PlayerType.HUMAN,sessionIDInput.text)
                hideOptions()
                shuffel.isVisible= true
                startButton.isVisible = true
            }
        }


    init {
        addComponents(backButton,headLine,nameInput,isBot,
            joinLobbyButton,createLobbyButton, shuffel, waiting,players,
            secret,secretInput,sessionID,sessionIDInput, startButton,disconnect)
        background = ColorVisual(0, 0, 0, 0)
        waiting.isVisible = false
        shuffel.isVisible = false
        startButton.isVisible = false
        disconnect.isVisible= false
        players.isDisabled= true
    }

    /**
     * hides/ disables input options
     */
    fun hideOptions(){
        joinLobbyButton.isVisible = false
        createLobbyButton.isVisible = false
        secretInput.isDisabled= true
        sessionIDInput.isDisabled= true
        nameInput.isDisabled= true
        isBot.isDisabled= true
        backButton.isVisible= false
        disconnect.isVisible= true
    }

    /**
     * shows/ enables input options
     */
    fun showOptions(){
        joinLobbyButton.isVisible = true
        createLobbyButton.isVisible = true
        secretInput.isDisabled= false
        sessionIDInput.isDisabled= false
        nameInput.isDisabled= false
        isBot.isDisabled= false
        backButton.isVisible= true
        disconnect.isVisible= false
        shuffel.isVisible= false
        startButton.isVisible = false
        waiting.isVisible = false
    }

    /**
     * add player names to refreshAfterNetworkPlayerJoined
     */
    override fun refreshAfterNetworkPlayerJoined(playerNames: List<String>) {
         names = playerNames.toMutableList()
         players.text= names.joinToString("\n")
    }
}