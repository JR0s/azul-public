package gui.scenes.subComponants

import entity.Color
import gui.VisualMode
import gui.scenes.builder.CenterValues
import gui.scenes.builder.SetVisual
import service.Refreshable
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual

/**
 * Player class that extends GameComponentView.
 * It represents a player in the game with position, size, rotation, and player number.
 * @param posX The x-coordinate of the player's position.
 * @param posY The y-coordinate of the player's position.
 * @param width The width of the player.
 * @param height The height of the player.
 */
class Player( posX : Number = 0, posY : Number = 0, width: Number = 321, height: Number = 234) : Refreshable,
    Pane<ComponentView>(posX, posY, width, height ), CenterValues, SetVisual {
    val icon = Label(width,0,50,50,"")
    val nameLabel = Label(width,50,50,35,"")
    val grid = GridPane<Tiles>(
            posX = 237,
            posY = 96,
            rows = 5,
            columns = 5,
            spacing = 3.2,
    )
    val patternLine = GridPane<Tiles>(
        posX = 83,
        posY = 96,
        rows = 5,
        columns = 5,
        spacing = 3,
    )
    val hideit= Label(
        posX = 169,
        posY = 28,
        137,137,
        visual= ColorVisual(256,256,265,0.2)
    )
    val komischeUntereReihe = GridPane<Tiles>(
        posX = 119,
        posY = 200,
        rows = 1,
        columns = 7,
        spacing = 5.5,
    )
    val label = Label(
        posX = 12,
        posY = 6,
        width = 28,
        height = 10,
        text = "0",
        font = Font(size = 9)
    )
    var fill=0


init {

    for(i in  0..4) {
        for(j in 0..4) {
            grid[i,j] = Tiles(
                0.0,
                0.0,
                Color.INVISIBLE )
        }
    }

    for(i in 0..4) {
        for(j in (0..4).reversed()) {
            patternLine[4-i,j] = Tiles(
                0.0,
                0.0,
                Color.INVISIBLE
            )
        }
    }

    for (i in 0..6) {
        komischeUntereReihe[i,0] = Tiles(
            0.0,
            0.0,
            Color.INVISIBLE
        )
    }

    addAll(hideit,patternLine, grid, komischeUntereReihe, label, icon, nameLabel)

    visual = gui.scenes.builder.VisKey.getVisual(gui.scenes.builder.VisKey.BOARD_SHORT, gui.AzulApplication.mode)


    }

    /**
     * returns the middle cords of a player
     */
    fun middleCord(): Pair<Double, Double> {
        return Pair(posX + width / 2-10, posY + height / 2-10)
    }

    override fun setVisual(mode: VisualMode) {

        for( i in patternLine)
            i.component?.setVisual(mode) ?: {}
        for(i in grid)
            i.component?.setVisual(mode) ?: {}

        for(i in patternLine)
            i.component?.setVisual(mode) ?: {}


    }


}
