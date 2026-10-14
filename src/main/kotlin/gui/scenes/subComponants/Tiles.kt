package gui.scenes.subComponants

import entity.Color
import gui.VisualMode
import gui.scenes.builder.SetVisual
import gui.scenes.builder.VisKey
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual

/**
 * Selected is a view displays the image of a tiles.
 */
class Tiles(x: Double, y: Double, color: Color): TokenView(x,y,25,25,
    when(color){
        Color.WHITE -> VisKey.getVisual(VisKey.WHITE, gui.AzulApplication.mode)
        Color.BLACK -> VisKey.getVisual(VisKey.BLACK, gui.AzulApplication.mode)
        Color.YELLOW -> VisKey.getVisual(VisKey.ORANGE, gui.AzulApplication.mode)
        Color.RED -> VisKey.getVisual(VisKey.RED, gui.AzulApplication.mode)
        Color.BLUE -> VisKey.getVisual(VisKey.BLUE, gui.AzulApplication.mode)
        Color.START_MARKER -> VisKey.getVisual(VisKey.PLAYER_TOKEN, gui.AzulApplication.mode)
        else -> ColorVisual(0, 0, 0, 0)
    }), SetVisual {
    var col = color
    /**
     * changes the color and image to the given color
     */
    fun setColor(color: Color?){
        if(color != null) {
            this.visual = when (color) {
                Color.WHITE -> VisKey.getVisual(VisKey.WHITE, gui.AzulApplication.mode)
                Color.BLACK -> VisKey.getVisual(VisKey.BLACK, gui.AzulApplication.mode)
                Color.YELLOW -> VisKey.getVisual(VisKey.ORANGE, gui.AzulApplication.mode)
                Color.RED -> VisKey.getVisual(VisKey.RED, gui.AzulApplication.mode)
                Color.BLUE -> VisKey.getVisual(VisKey.BLUE, gui.AzulApplication.mode)
                Color.START_MARKER -> VisKey.getVisual(VisKey.PLAYER_TOKEN, gui.AzulApplication.mode)
                else -> ColorVisual(0, 0, 0, 0)
            }
            col = color
        }else{
            this.visual =  ColorVisual(0, 0, 0, 0)
            col = Color.INVISIBLE
        }
    }

    /**
     * returns the color of the tile
     */
    fun color(): Color {
        return col
    }

    override fun setVisual(mode: VisualMode) {

       val visKeyFromColor = when(this.col){
            Color.WHITE -> VisKey.WHITE
            Color.BLACK -> VisKey.BLACK
            Color.YELLOW -> VisKey.ORANGE
            Color.RED -> VisKey.RED
            Color.BLUE -> VisKey.BLUE
            Color.START_MARKER -> VisKey.PLAYER_TOKEN
            else -> VisKey.EMPTY
        }


        this.visual = VisKey.getVisual(visKeyFromColor, mode)!!
    }
}