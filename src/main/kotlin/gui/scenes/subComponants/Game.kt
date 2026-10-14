package gui.scenes.subComponants

import gui.VisualMode
import gui.scenes.builder.SetVisual
import gui.scenes.builder.center
import gui.scenes.builder.circle
import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.visual.ColorVisual

/**
 * Game.kt
 * Holds the main gmae components of the game
 * It is the commponent the camaraplane usus to displa y the board
 */
class Game(val rootService: RootService) : Pane<Pane<*>>(posX = 0, 0, 1920,1080 ), Refreshable, SetVisual {

    val menuButton: Button = Button(1650, 100, 100, 35, "Menu")
        .apply { visual = ColorVisual(128, 128, 128) }
    val undo: Button = Button(1650, 150, 100, 35, "Undo")
        .apply { visual = ColorVisual(128, 128, 128)
        onMouseClicked ={
            rootService.playerActionService.undo()
        }}
    val redo: Button = Button(1650, 200, 100, 35, "Redo")
        .apply { visual = ColorVisual(128, 128, 128)
        onMouseClicked ={
            rootService.playerActionService.redo()
        }}
    val selected: Selected = Selected()
    val center: Center = Center(posX = 0,
        posY = 0,
        width = 300,
        height = 300,
        ammount = 5)

    var players: MutableList<Player> = mutableListOf()

    init {
        addAll(players)
        addAll(center)

        val centerCords = center.center(width / 2.0, height / 2.0)
        center.reposition(centerCords.first, centerCords.second)
    }

    /**
     * Creates the players and other componants for the game
     *
     * @param amountPlayer the amount of players in the game
     *
     */
    fun create(amountPlayer: Int) {
        players=mutableListOf()
        val playerPos = circle(amountPlayer, width/5, width / 2, height / 2)
        for (i in playerPos) {
            val player = Player(posX = i.y, posY = i.x)
            val center = player.center(i.x, i.y)
            player.reposition(center.first, center.second)
            player.rotation = i.rotation
            add(player)
            players.add(player)
            val pos = players.lastIndex
            for (i in 0..4){
                for(j in (i .. 4).reversed()){
                    player.patternLine[4-i,j]?.onMouseClicked ={
                        rootService.playerActionService.placeTilesOntoBoard(pos,j)
                    }
                }
            }
            for(i in 0..6){
                player.komischeUntereReihe[i,0]?.onMouseClicked={
                    rootService.playerActionService.placeTilesOntoFloorLine(pos)
                }
            }
        }
        val amount = when(amountPlayer) {
            2-> {5}
            3-> {7}
            else ->{9}
        }
        remove(center)
        center.createFactorys(center.width/2, center.height / 2, amount)
        add(center)

    }

    override fun setVisual(mode: VisualMode) {
        for (i in players) {
            i.setVisual(mode)
        }
        center.setVisual(mode)
        selected.setVisual(mode)
    }
}