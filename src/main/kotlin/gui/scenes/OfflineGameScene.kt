package gui.scenes

import entity.Color
import entity.GameState
import entity.PlayerType
import gui.AzulApplication
import gui.Sounds
import gui.VisualMode

import gui.scenes.builder.VisKey
import gui.scenes.subComponants.Game
import gui.scenes.subComponants.Player
import gui.scenes.subComponants.Tiles
import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.components.layoutviews.CameraPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

import tools.aqua.bgw.visual.Visual
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

/**
 * GameScene is the main scene for the Azul game.
 * It holds the game animations and interactions.
 */

class OfflineGameScene(val rootService: RootService): BoardGameScene(1920,1080), Refreshable {
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
        addComponents(game.menuButton, game.redo,game.undo, cameraPane,nextPlayer, speedButton)
        game.menuButton.toFront()
        game.redo.toFront()
        game.undo.toFront()
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
     * Animation, die Tiles in die mitte bringt
     */
    fun movetoMiddle(view: Tiles, toX: Double, toY: Double, view2 : Tiles) =
        MovementAnimation(view, toX - view.actualPosX, toY - view.actualPosY, 300).apply {
            onFinished = {
                view2.isVisible= true
                this.componentView.rotate(0.01)
                this.componentView.isVisible = false
            }
        }

    /**
     * Animation, die Tiles nach selected bringt
     */
    fun movetoSelected(view: Tiles, toX: Double, toY: Double, view2: Tiles) =
        MovementAnimation(view, toX - view.actualPosX, toY - view.actualPosY, 500).apply {
            onFinished = {
                view2.isVisible= true
                this.componentView.rotate(0.01)
                this.componentView.isVisible = false
                game.center.middle.views.remove(view)
            }
        }

    /**
     * Animation, die Tiles von selected in die FloorLine bewegt
     */
    fun movefromSelectedtoFloorline(view: Tiles, toX: Double, toY: Double, player: Player, line: Int, row: Int) =
        MovementAnimation(view, toX - view.actualPosX, toY - view.actualPosY, 800).apply {
            onFinished = {
                view.isVisible = false
                player.komischeUntereReihe[line, row]?.setColor(this.componentView.color())
                game.selected.removeView(view)
            }
        }

    /**
     * Animation, die den Startmarker in die FloorLine bewegt
     */
    fun movePlayerMarker(view: Tiles, toX: Double, toY: Double, player: Player, line: Int, row: Int) =
        MovementAnimation(view, toX - view.actualPosX, toY - view.actualPosY, 300).apply {
            onFinished = {
                view.isVisible = false
                player.komischeUntereReihe[line, row]?.isVisible= true
                game.center.middle.views.remove(view)
            }
        }

    /**
     * Animation, die Tiles von selected in die PatternLine bewget
     */
    fun movefromSelectedtoPatternline(view: Tiles, toX: Double, toY: Double, player: Player, row: Int, line: Int) =
        MovementAnimation(view, toX - view.actualPosX, toY - view.actualPosY, 800).apply {
            onFinished = {
                view.isVisible = false
                player.patternLine[row, line]?.setColor(this.componentView.color())
                game.selected.removeView(view)
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
    fun setSelected(){
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        val helper= azulGame.takenTiles
        if (helper.second==game.selected.views.size) {
            for (i in game.selected.views.indices) {
                if (game.selected.views[i].color() != helper.first) {
                    game.selected.views[i].setColor(helper.first)
                }
            }
        }else{
            game.selected.removeall()
            game.selected.posx=50.0
            (1 until helper.second).map{
                game.selected.addView(helper.first)
                addComponents(game.selected.views.last())
            }
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
        if(AzulApplication.mode == VisualMode.ROTTEN)
            Sounds().playSound(Sounds.SoundKey.STARTGAMESOUND)
        AzulApplication.showGameScene(AzulApplication.Offliegame)
        AzulApplication.hideMenuScene()
        game.create(amountPlayer)
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
                    PlayerType.HUMAN -> VisKey.getVisual(VisKey.HUMAN, AzulApplication.mode)
                    PlayerType.BOT_RANDOM, PlayerType.BOT_PRO -> VisKey.getVisual(VisKey.BOT, AzulApplication.mode)
                    PlayerType.ONLINE -> VisKey.getVisual(VisKey.ONLINE, AzulApplication.mode)
                    else -> Visual.EMPTY
                }
        }
        visuals()

    }

    /**
     * factories befüllen und PlayerMarker in die Mitte
     */
    override fun refreshAfterStartNewRound() {
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        setFactorys()
        setMiddle()
        setSelected()
        val degree = -game.players[azulGame.currentPlayer].rotation
        game.rotation=degree
        rootService.gameService.startNewTurn()
    }

    /**
     * Tiles aus der Mitte nehmen
     */
    override fun refreshAfterTakeTilesFromMiddle(color: Color) {
        println("take mid")
        var tox = 50.0
        game.selected.posx = 50.0
        for (i in 0..game.center.middle.views.lastIndex) {
            if (game.center.middle.views[i].color() == Color.START_MARKER) {
                val azulGame = rootService.currentGame?.azulGameState
                checkNotNull(azulGame)
                val filled = game.players[azulGame.currentPlayer].fill
                val player: Player = game.players[azulGame.currentPlayer]
                val tile =player.komischeUntereReihe[filled, 0]
                checkNotNull(tile)
                val x = tile.actualPosX
                val y = tile.actualPosY
                val result = rotateAround(x,y,player.middleCord().first,
                    player.middleCord().second,-player.rotation)
                player.komischeUntereReihe[filled, 0]?.isVisible=false
                player.komischeUntereReihe[filled, 0]?.setColor(Color.START_MARKER)
                playAnimation(movePlayerMarker(game.center.middle.views[i],
                    result.first+player.middleCord().first,
                    result.second+player.middleCord().second, player, filled, 0))
                game.players[azulGame.currentPlayer].fill += 1
            }
            if (game.center.middle.views[i].color() == color) {
                val result = rotateAround(tox,50.0,950.0,530.0,game.rotation)
                game.selected.addView(color)
                game.selected.views.last().isVisible=false
                addComponents(game.selected.views.last())
                playAnimation(movetoSelected(game.center.middle.views[i],
                    result.first+950, result.second+530,game.selected.views.last()))
                tox += 30
            }

        }
        playAnimation(DelayAnimation(600).apply {  onFinished={
            setSelected()
            setMiddle()
        }})
        println(game.selected.used)
    }

    /**
     * Tiles aus der Factory nehmen
     */
    override fun refreshAfterTakeTilesFromFactory(color: Color, factoryIndex: Int, takenCount: Int) {
        println("take fac")
        val factory = game.center.factorys.factoryList[factoryIndex]
        val tiles = factory.tiles
        var tox = 50.0
        val toy = 50.0
        game.selected.posx = 50.0
        for (i in 0..tiles.lastIndex) {
            if (tiles[i].color() == color) {

                val result = rotateAround(tox, toy, 950.0, 530.0, game.rotation)
                val result2 = rotateAround(
                    result.first + 950, result.second + 530, factory.actualPosX + 13,
                    factory.actualPosY + 13, factory.rotation
                )
                game.selected.addView(color)
                game.selected.views.last().isVisible=false
                addComponents(game.selected.views.last())
                playAnimation(
                    movetoSelected(
                        tiles[i], result2.first + factory.actualPosX + 13,
                        result2.second + factory.actualPosY + 13,game.selected.views.last()
                    )
                )

                tox += 30
            } else {
                var x = 960.0+ Random.nextDouble(-30.0,30.0)
                var y = 540.0+ Random.nextDouble(-30.0,30.0)
                when(tiles[i].color()){
                    Color.YELLOW ->{ x += 0;y += 55 }
                    Color.RED -> { x+= -71.3;y+= 23.2 }
                    Color.BLUE -> { x+= -43.1;y+= -60.67 }
                    Color.WHITE -> { x+= 44.08;y+= -60.67 }
                    Color.BLACK -> { x+= 51.3;y+= 23.2 }
                    else -> { IllegalStateException("keine Farbe") }

                }
                val result = rotateAround(
                    x, y, factory.actualPosX + 13, factory.actualPosY + 13, factory.rotation
                )
                val view2 = Tiles(x-game.center.middle.actualPosX,
                    y-game.center.middle.actualPosY, tiles[i].color()).apply {
                    onMouseClicked = {
                        rootService.playerActionService.takeTilesFromMiddle(this.color())
                    }
                    rotation = tiles[i].rotation
                    isVisible= false
                }
                game.center.addit(view2)

                playAnimation(
                    movetoMiddle(
                        tiles[i], result.first+factory.actualPosX + 13.0,
                        result.second+factory.actualPosY+13, view2
                    )
                )

            }
        }
        playAnimation(DelayAnimation(600).apply {  onFinished={
            setSelected()
            setMiddle()
        }})
        if(AzulApplication.mode == VisualMode.ROTTEN)
            Sounds().playSound(Sounds.SoundKey.TILE_TAKEN)
    }

    /**
     * verschiebt Tiles von Selected in die PatternLines
     */
    override fun refreshAfterPlaceTilesInPatternLine(playerNr: Int, line: Int, amount: Int) {
        val player = game.players[playerNr]
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        var filled = azulGame.players[playerNr].board.patternLines[line].fill - amount
        game.selected.used = amount
        for (i in 1..amount) {
            val tile = player.patternLine[4 - filled, line]
            checkNotNull(tile)
            val x = tile.actualPosX
            val y = tile.actualPosY
            val view = game.selected.views[game.selected.views.lastIndex - i + 1]
            view.isVisible=true
            val result = rotateAround(x,y,player.middleCord().first,
                player.middleCord().second,-player.rotation)
            val result2 = rotateAround(result.first+player.middleCord().first,
                result.second+player.middleCord().second,950.0,530.0,-game.rotation)
            playAnimation(
                movefromSelectedtoPatternline(
                    view, result2.first+950, result2.second+530, player, 4 - filled, line
                )
            )
            filled += 1
        }
        //row = 0 nur für Testen
        playAnimation(DelayAnimation(1000).apply {onFinished={
            rootService.gameService.endTurn(line)
        }})
        if(AzulApplication.mode == VisualMode.ROTTEN)
            Sounds().playSound(Sounds.SoundKey.TILE_PATTERNLINE)
    }

    /**
     * verschiebt Tiles von selected in die FloorLine
     */
    override fun refreshAfterPlaceTilesInFloorLine(playerNr: Int, amount: Int) {
        val player = game.players[playerNr]
        for (i in 1..amount) {
            val filled = game.players[playerNr].fill
            val tile =player.komischeUntereReihe[filled, 0]
            checkNotNull(tile)
            val result = rotateAround(tile.actualPosX,tile.actualPosY,
                player.middleCord().first,player.middleCord().second,-player.rotation)
            val result2 = rotateAround(result.first+player.middleCord().first,
                result.second+player.middleCord().second, 950.0,530.0,-game.rotation)
            val view = game.selected.views[game.selected.views.lastIndex - i + 1-game.selected.used]
            view.isVisible=true
            playAnimation(
                movefromSelectedtoFloorline(
                    view,result2.first+950, result2.second+530, player, filled, 0
                )
            )
            game.players[playerNr].fill += 1
        }

        //row = 0 nur für Testen
        playAnimation(DelayAnimation(1000).apply {onFinished={
            if (rootService.currentGame?.azulGameState?.state == GameState.END_TURN) {
                rootService.gameService.endTurn(-1)
            }
        }})
        game.selected.posx=50.0
        if(AzulApplication.mode == VisualMode.ROTTEN)
            Sounds().playSound(Sounds.SoundKey.TILE_FLOORLINE)
    }

    /**
     * game wird passen gedreht
     */
    override fun refreshAfterNewTurn() {
        if(AzulApplication.mode == VisualMode.ROTTEN)
            Sounds().playSound(Sounds.SoundKey.START_ROUND)

        if((5 == nextInt(0,10))&& AzulApplication.mode == VisualMode.ROTTEN)
            Bang()


        game.selected.used=0
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        nextPlayer.text=azulGame.players[azulGame.currentPlayer].name+"'s Turn"
        nextPlayer.isVisible=true
        playAnimation(DelayAnimation(600).apply { onFinished={
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
        azulGame.usedTiles
        for (i in 0..game.players.size - 1) {
            playAnimation(DelayAnimation(50).apply {onFinished={
                setPatternLines(i)
                setGrid(i)
                setFloorLine(i)
            }})
            game.players[i].fill =0
            game.players[i].label.text=azulGame.players[i].score.toString()
        }
        playAnimation(DelayAnimation(100).apply {onFinished={
            rootService.gameService.startNewRound()
        }})
        if(AzulApplication.mode == VisualMode.ROTTEN)
            Sounds().playSound(Sounds.SoundKey.START_ROUND)
    }

    /**
     * dreht Game
     * prüft ob darstellung korrekt
     */
    override fun refreshAfterEndTurn() {
        game.selected.used=0
        setSelected()
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        playAnimation(DelayAnimation(300).apply {onFinished={
            game.rotation=-game.players[azulGame.currentPlayer].rotation
            rootService.gameService.startNewTurn()
        }})
        for (i in 0..game.players.size - 1) {
            playAnimation(DelayAnimation(50).apply {onFinished={
                setPatternLines(i)
                setFloorLine(i)
            }})
        }
        game.undo.isVisible=true
    }

    /**
     * updatet die view nach dem Laden
     */
    override fun refreshAfterLoadGame() {
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        refreshAfterStartGame(azulGame.players.size)
        loadAzulGamsState()
    }

    /**
     * updatet die View nach Redo
     */
    override fun refreshAfterRedo(){
        val azulGame = rootService.currentGame
        checkNotNull(azulGame)
        game.undo.isVisible=true
        if (azulGame.redoStack.isEmpty()){
            game.redo.isVisible=false
        }
        loadAzulGamsState()
    }

    /**
     * updatet die View nach Undo
     */
    override fun refreshAfterUndo() {
        val azulGame = rootService.currentGame
        checkNotNull(azulGame)
        game.redo.isVisible=true
        if (azulGame.undoStack.isEmpty()){
            game.undo.isVisible=false
        }
        loadAzulGamsState()
    }

    /**
     * updaten der Game View
     */
    fun loadAzulGamsState(){
        for (i in game.players.indices){
            setPatternLines(i)
            setFloorLine(i)
            setGrid(i)
        }
        setFactorys()
        setSelected()
        setMiddle()
        val azulGame = rootService.currentGame?.azulGameState
        checkNotNull(azulGame)
        val degree = -game.players[azulGame.currentPlayer].rotation
        game.rotation=degree
    }

    /**
     * rotiert Koordinaten um andere Koordinaten anhand eines Winkels
     */
    private fun rotateAround(x:Double, y:Double, xpos:Double, ypos:Double, rotation: Double): Pair<Double, Double> {
        val xm = x-xpos
        val ym = y-ypos
        val posx =(xm*cos((-rotation)/(180/ PI)) - ym*sin((-rotation)/(180/ PI)))
        val posy =(xm*sin((-rotation)/(180/ PI)) + ym*cos((-rotation)/(180/ PI)))
        return Pair(posx,posy)
    }


    override fun refreshAfterSpeedSet(speed: Int) {
        speedButton.text = "Speed: ${speed}s"
    }
    override fun refreshAfterVisualselection(){
        visuals()
    }
     fun visuals(){
        val visualMode = checkNotNull(AzulApplication.mode)
        this.background  = VisKey.getVisual(VisKey.BACKGROUND, visualMode)
        for(i in game.players.indices){
            game.players[i].setVisual(visualMode)
        }
        game.center.setVisual(visualMode)
        game.selected.setVisual(visualMode)
    }
}