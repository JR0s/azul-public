package gui.scenes


import entity.PlayerType
import gui.AzulApplication
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import gui.VisualMode

/**
 * OfflineMainMenuScene is the scene shown when the game is started in offline mode.
 * It allows the user to add players and bots before starting the game.
 */

class OfflineMainMenuScene(rootService: RootService) : MenuScene(500, 600), Refreshable {

    var players : Int = 0

    val titel = Label(
        120, 50, 200, 50, "Add Player"
    ).apply { font = Font(size = 22) }

    private val p1Bot = Label(width = 35, height = 35, posX = 15, posY = 125, text = "").apply {
       visual = gui.scenes.builder.VisKey.getVisual(gui.scenes.builder.VisKey.BOT, gui.AzulApplication.mode)
    }
    private val p1Label = Label(width = 80, height = 35, posX = 50, posY = 125, text = "Player 1:")
    private val p1Input: TextField = TextField(width = 200, height = 35, posX = 130, posY = 125, text = "")
    private val p1Delete = Button(width = 35, height = 35, posX = 340, posY = 125, text = "X").apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked={remove(1)}
    }

    private val p2Bot = Label(width = 35, height = 35, posX = 15, posY = 170, text = "").apply {
        visual = gui.scenes.builder.VisKey.getVisual(gui.scenes.builder.VisKey.BOT, gui.AzulApplication.mode)
    }
    private val p2Label = Label(width = 80, height = 35, posX = 50, posY = 170, text = "Player 2:")
    private val p2Input: TextField = TextField(width = 200, height = 35, posX = 130, posY = 170, text = "")
    private val p2Delete = Button(width = 35, height = 35, posX = 340, posY = 170, text = "X").apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked={remove(2)}
    }

    private val p3Bot = Label(width = 35, height = 35, posX = 15, posY = 215, text = "").apply {
        visual = gui.scenes.builder.VisKey.getVisual(gui.scenes.builder.VisKey.BOT, gui.AzulApplication.mode)
    }
    private val p3Label = Label(width = 80, height = 35, posX = 50, posY = 215, text = "Player 3:")
    private val p3Input: TextField = TextField(width = 200, height = 35, posX = 130, posY = 215, text = "")
    private val p3Delete = Button(width = 35, height = 35, posX = 340, posY = 215, text = "X").apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked={remove(3)}
    }

    private val p4Bot = Label(width = 35, height = 35, posX = 15, posY = 260, text = "").apply {
        visual = gui.scenes.builder.VisKey.getVisual(gui.scenes.builder.VisKey.BOT, gui.AzulApplication.mode)
    }
    private val p4Label = Label(width = 80, height = 35, posX = 50, posY = 260, text = "Player 4:")
    private val p4Input: TextField = TextField(width = 200, height = 35, posX = 130, posY = 260, text = "")
    private val p4Delete = Button(width = 35, height = 35, posX = 340, posY = 260, text = "X").apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked={remove(4)}
    }

    val botButton = Button(width = 80, height = 40, posX = 280, posY = 320, text = "add Bot").apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked={ addBot() }
    }

    val backButton = Button(width = 100, height = 35, posX = 50, posY = 550, text = "go back").apply {
        visual = ColorVisual(221, 136, 136)
    }

    val playerButton = Button(width = 80, height = 40, posX = 120, posY = 320, text = "add Player").apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked={ addPlayer() }

    }
    val shuffel = CheckBox(175,400,150,35,"Shuffel Players")

    val startButton = Button(width = 100, height = 35, posX = 400, posY = 550, text = "Start").apply {
        visual = ColorVisual(221, 136, 136)
        onMouseClicked= {
            rootService.removeRefreshable(AzulApplication.Offliegame)
            rootService.addRefreshable(AzulApplication.Offliegame)
            rootService.removeRefreshable(AzulApplication.Onliegame)
            rootService.gameService.startNewGame(
                false, getPlayer()
            )
        }
    }

    val inputs = listOf(p1Input,p2Input, p3Input, p4Input)
    val bots = listOf(p1Bot,p2Bot, p3Bot, p4Bot)

    val loadButton = Button(width = 100, height = 35, posX = 400, posY = 500, text = "Load").apply {
        visual = ColorVisual(221, 136, 136)
    }

    /**
     * initialize the components
     */
    init {
        addComponents(
            backButton,botButton,playerButton,titel,
            startButton,loadButton,p1Label,p1Input,p2Label,p2Input,p2Delete,
            p1Bot,p1Delete,p3Label,p3Input,p3Delete,p4Label,p4Input,p4Delete,
            p2Bot,p3Bot,p4Bot,shuffel
        )
        startButton.isVisible=false
        p1Input.isVisible = false
        p1Label.isVisible = false
        p1Delete.isVisible = false
        p2Input.isVisible = false
        p2Label.isVisible = false
        p2Delete.isVisible = false
        p3Input.isVisible = false
        p3Label.isVisible = false
        p3Delete.isVisible = false
        p4Input.isVisible = false
        p4Label.isVisible = false
        p4Delete.isVisible = false
        p1Bot.isVisible = false
        p2Bot.isVisible = false
        p3Bot.isVisible = false
        p4Bot.isVisible = false
        background = ColorVisual(0, 0, 0, 0)
    }

    /**
     * creates the player List
     */
    fun getPlayer(): Array<Pair<String, PlayerType>> {
        val result = Array(players) { i ->
            val playerName = inputs[i].text
            val playerType = if( !bots[i].isVisible) PlayerType.HUMAN else PlayerType.BOT_RANDOM
            Pair(playerName, playerType)
        }
        if (shuffel.isChecked){
            result.shuffle()
        }
        return result
    }

    /**
     * add a bot to visual
     */
    fun addBot() {
        when(players){
            0-> {
                p1Label.isVisible = true
                p1Input.isVisible = true
                p1Delete.isVisible = true
                p1Bot.isVisible = true
            }
            1-> {
                p2Label.isVisible = true
                p2Input.isVisible = true
                p2Delete.isVisible = true
                startButton.isVisible = true
                p2Bot.isVisible = true
            }
            2-> {
                p3Label.isVisible = true
                p3Input.isVisible = true
                p3Delete.isVisible = true
                p3Bot.isVisible = true

            }
            3-> {
                p4Label.isVisible = true
                p4Input.isVisible = true
                p4Delete.isVisible = true
                p4Bot.isVisible = true
                botButton.isVisible = false
                playerButton.isVisible = false
            }
            else -> IllegalArgumentException("Es dürfen keine weiteren Bots hinzugefügt werden")
        }
        players+=1
    }

    /**
     * add a player to visual
     */
    fun addPlayer() {
        when(players){
            0-> {
                p1Label.isVisible = true
                p1Input.isVisible = true
                p1Delete.isVisible = true
            }
            1-> {
                p2Label.isVisible = true
                p2Input.isVisible = true
                p2Delete.isVisible = true
                startButton.isVisible = true
            }
            2-> {
                p3Label.isVisible = true
                p3Input.isVisible = true
                p3Delete.isVisible = true

            }
            3-> {
                p4Label.isVisible = true
                p4Input.isVisible = true
                p4Delete.isVisible = true
                botButton.isVisible = false
                playerButton.isVisible = false
            }
            else -> IllegalArgumentException("Es dürfen keine weiteren Player hinzugefügt werden")
        }
        players+=1
    }

    /**
     * remove player position from view
     */
    fun remove(position: Int) {
        when(position){
            0->IllegalArgumentException("No Player to remove.")
            1-> {
                if (players==position){
                    p1Label.isVisible = false
                    p1Input.isVisible = false
                    p1Input.text=""
                    p1Delete.isVisible = false
                    players-=1
                    p1Bot.isVisible = false
                }else{
                    p1Input.text=p2Input.text
                    p1Bot.isVisible = p2Bot.isVisible
                    remove(position+1)
                }
            }
            2-> {
                if (players==position){
                    p2Label.isVisible = false
                    p2Input.isVisible = false
                    p2Input.text=""
                    p2Delete.isVisible = false
                    players-=1
                    startButton.isVisible = false
                    p2Bot.isVisible = false
                }else{
                    p2Input.text=p3Input.text
                    p2Bot.isVisible = p3Bot.isVisible
                    remove(position+1)
                }
            }
            3-> {
                if (players==position){
                    p3Label.isVisible = false
                    p3Input.isVisible = false
                    p3Input.text=""
                    p3Delete.isVisible = false
                    players-=1
                    p3Bot.isVisible = false
                }else{
                    p3Input.text=p4Input.text
                    p3Bot.isVisible = p4Bot.isVisible
                    remove(position+1)
                }
            }
            4-> {
                p4Label.isVisible = false
                p4Input.isVisible = false
                p4Input.text=""
                p4Delete.isVisible = false
                botButton.isVisible = true
                playerButton.isVisible = true
                players-=1
                p4Bot.isVisible = false
            }
        }
    }
}