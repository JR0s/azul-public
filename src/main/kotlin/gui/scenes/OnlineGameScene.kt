package gui.scenes

import entity.Color
import entity.PlayerType
import gui.VisualMode
import gui.AzulApplication

import gui.scenes.builder.VisKey
import gui.scenes.subComponants.Game
import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.layoutviews.CameraPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import kotlin.random.Random

/**
 * GameScene is the main scene for the Azul game.
 * It holds the game animations and interactions.
 */

class OnlineGameScene(val rootService: RootService): BoardGameScene(1920,1080), Refreshable {
    var game = Game(rootService)
    var speedButton = Button(100, 400, 200, 40, "Speeeeeeeed", visual = ColorVisual(0,0,0,100)).apply {
        onMouseClicked = {
            rootService.playerActionService.speedSet()
        }
    }
    private val cameraPane = CameraPane(
        0,
        0,
        1920,
        1080,
        Visual.EMPTY,
        target = game,
        limitBounds = true,
    ).apply { interactive = false }

    val nextPlayer = Label(840,510,240,60,"",
        visual=ColorVisual(128, 128, 128,0.4), font = Font(size = 35))

    val names : MutableList<String> = mutableListOf()
    val buttons : MutableList<Button> = mutableListOf()

    init {
        addComponents( cameraPane,nextPlayer,speedButton)
        game.undo.isVisible=false
        game.redo.isVisible=false
        nextPlayer.isVisible = false
        background = VisKey.getVisual(VisKey.BACKGROUND, AzulApplication.mode)
    }

    /**
     * Funktion to shop a close up of the boards
     */
    fun showBoard(index:Int){
        cameraPane.pan(1020,920,4.0)
        val degree = -game.players[index].rotation
        game.rotation=degree
        for(i in buttons.indices){
            buttons[i].apply {
                text="Zoom on ${names[i]}"
                onMouseClicked={
                    showBoard(i)
                }
            }
        }
        buttons[index].apply {
            text="Zoom out"
            onMouseClicked={
                showGame(index)
            }
        }
    }

    /**
     * Function to zoom out again
     */
    fun showGame(index:Int){
        cameraPane.pan(0,0,1.0)
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        val degree = -game.players[azulGame.currentPlayer].rotation
        game.rotation=degree
        buttons[index].apply {
            text="Zoom on ${names[index]}"
            onMouseClicked={
                showBoard(index)
            }
        }
    }

    /**
     * Updatet die PatternLine des players
     */
    fun setPatternLines(playerNr: Int){
        val player = game.players[playerNr]
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        for (i in 0..4){
            val patternLine = azulGame.players[playerNr].board.patternLines[i]
            for (j in 0..i){
                if (j<=patternLine.fill-1) {
                    player.patternLine[4 - j, i]?.setColor(patternLine.color)
                }else{
                    player.patternLine[4 - j, i]?.setColor(Color.INVISIBLE)
                }
            }
        }
    }

    /**
     * Updatet die FloorLine des Players
     */
    fun setFloorLine(playerNr: Int){
        val player = game.players[playerNr]
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        val floorLine =azulGame.players[playerNr].board.floorLine
        for (i in 0..6){
            if (i<= floorLine.size-1) {
                player.komischeUntereReihe[i, 0]?.setColor(floorLine[i])
            }else{
                player.komischeUntereReihe[i, 0]?.setColor(Color.INVISIBLE)
            }
        }
    }

    /**
     * updatet selected, wenn die darstellung fehlerhaft ist
     */
    fun setSelected() {
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        val helper = azulGame.takenTiles
        game.selected.removeall()
        game.selected.posx = 50.0
        repeat(helper.second) {
            game.selected.addView(helper.first)
            addComponents(game.selected.views.last())
        }
    }

    /**
     * updatet das Grid eines players
     */
    fun setGrid(playerNr: Int){
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        for (i in 0..4){
            for (j in 0..4){
                val tile = azulGame.players[playerNr].board.wall[i][j]
                if (tile.filled){
                    game.players[playerNr].grid[j,i]?.setColor(tile.color)
                }else{
                    game.players[playerNr].grid[j,i]?.setColor(Color.INVISIBLE)
                }
            }
        }
    }

    /**
     * updatet middle, wenn die Darstellung fehlerhaft ist
     */
    fun setMiddle(){
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        var farbe= false
        for (color in Color.entries.filter { it!= Color.INVISIBLE }){
            if (game.center.middle.views.count { it.color() == color } != azulGame.middle.tiles.count { it == color }) {
                farbe= true
            }
        }
        if (game.center.middle.views.size!=azulGame.middle.tiles.size||farbe){
            game.center.middle.removeall()
            game.center.setmiddle(azulGame.middle)
            game.center.middle.toFront()
            setMiddleHelper()
        }
    }

    /**
     * Hilft setMiddle bei Laden, da sonst zu tief
     */
    fun setMiddleHelper(){
        for (i in game.center.middle.views.indices){
            if ( game.center.middle.views[i].color()!=Color.START_MARKER)
                game.center.middle.views[i].apply { onMouseClicked = {
                    rootService.playerActionService.takeTilesFromMiddle(this.color())
                }}
        }
    }

    /**
     * updatet die Factories beim Laden oder neuer Runde
     */
    fun setFactorys(){
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        for (i in game.center.factorys.factoryList.indices) {
            val factory = game.center.factorys.factoryList[i]
            for (j in 0..3) {
                setFactorysHelper(factory,i,j)
            }
        }
    }

    /**
     * Hilft setFactorys bei Laden, da sonst zu tief
     */
    private fun setFactorysHelper(factory: gui.scenes.subComponants.Factory, i:Int ,j:Int){
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        if (j <= azulGame.factories[i].tiles.size) {
            val color: Color = azulGame.factories[i].tiles[j]
            factory.tiles[j].apply {
                onMouseClicked = {
                    rootService.playerActionService.takeTilesFromFactory(i, this.color())
                }
                setColor(color)
                rotation = Random.nextDouble(0.0, 360.0)
                isVisible=true
            }
        }
    }

    /**
     * spiel erstellung
     */
    override fun refreshAfterStartGame(amountPlayer: Int) {
        AzulApplication.showGameScene(AzulApplication.Onliegame)
        AzulApplication.hideMenuScene()
        game.create(amountPlayer)
        game.center.middle.removeall()
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        for (i in game.players.indices) {
            names.add(azulGame.players[i].name)
        }
        val zoomBoards = (0 until game.players.size).map {
            Button(
                posX = 1600, posY = 400 + it * 50, width = 200, height = 30,
                text = "Zoom on ${names[it]}"
            )
        }
        zoomBoards.zip(0 until game.players.size).forEach { (button, index) ->
            run {
                button.onMouseClicked = {
                    showBoard(index)
                }
                addComponents(button)
                buttons.add(button)
            }
        }
        for (i in game.players.indices) {
            game.players[i].nameLabel.text=azulGame.players[i].name
            game.players[i].icon.visual=
                when(azulGame.players[i].playerType){
                    PlayerType.HUMAN -> ImageVisual("normal/Human.png")
                    PlayerType.BOT_RANDOM, PlayerType.BOT_PRO -> ImageVisual("normal/Bot.png")
                    PlayerType.ONLINE -> ImageVisual("normal/Online.png")
                    else -> Visual.EMPTY
                }
        }
    }

    /**
     * factories befüllen und PlayerMarker in die Mitte
     */
    override fun refreshAfterStartNewRound() {
        setMiddle()
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        setFactorys()
        val degree = -game.players[azulGame.currentPlayer].rotation
        game.rotation=degree
        println("ÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖÖRE")
        rootService.gameService.startNewTurn()
    }

    /**
     * Tiles aus der Mitte nehmen
     */
    override fun refreshAfterTakeTilesFromMiddle(color: Color) {
        setSelected()
        setMiddle()
    }

    /**
     * Tiles aus der Factory nehmen
     */
    override fun refreshAfterTakeTilesFromFactory(color: Color, factoryIndex: Int, takenCount: Int) {
        val factory = game.center.factorys.factoryList[factoryIndex]
        val tiles = factory.tiles
        for (i in tiles) {
            i.setColor(Color.INVISIBLE)
        }
        setSelected()
        setMiddle()
    }

    /**
     * verschiebt Tiles von Selected in die PatternLines
     */
    override fun refreshAfterPlaceTilesInPatternLine(playerNr: Int, line: Int, amount: Int) {
        setPatternLines(playerNr)
    }

    /**
     * verschiebt Tiles von selected in die FloorLine
     */
    override fun refreshAfterPlaceTilesInFloorLine(playerNr: Int, amount: Int) {
        setFloorLine(playerNr)
    }

    /**
     * game wird passen gedreht
     */
    override fun refreshAfterNewTurn() {

        if(AzulApplication.mode == VisualMode.ROTTEN)

        setSelected()
        setMiddle()
        setFactorys()
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        nextPlayer.text=azulGame.players[azulGame.currentPlayer].name+"'s Turn"
        nextPlayer.isVisible=true
        playAnimation(DelayAnimation(300).apply { onFinished={
            nextPlayer.text=""
            nextPlayer.isVisible = false
        }})
    }

    /**
     * bereitet neue Runde vor
     */
    override fun refreshAfterEndRound(currentPlayer: Int) {
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        for (i in 0..game.players.size - 1) {
                setPatternLines(i)
                setGrid(i)
                setFloorLine(i)
            game.players[i].fill =0
            game.players[i].label.text=azulGame.players[i].score.toString()
        }
            //rootService.gameService.startNewRound()
    }

    /**
     * dreht Game
     * prüft ob darstellung korrekt
     */
    override fun refreshAfterEndTurn() {
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
            game.rotation=-game.players[azulGame.currentPlayer].rotation
            rootService.gameService.startNewTurn()
        for (i in 0..game.players.size - 1) {
                setPatternLines(i)
                setFloorLine(i)
        }
        game.undo.isVisible=true
    }
    override fun refreshAfterSpeedSet(speed: Int) {
        speedButton.text = "Speed: ${speed}s"
    }
}