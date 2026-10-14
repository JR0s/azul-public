package gui.scenes.subComponants

import entity.Color
import gui.VisualMode
import gui.scenes.builder.SetVisual
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual

/**
 * Selected is a view that holds the selected tiles.
 */
class Selected: TokenView(50,50,100,25,
    visual = ColorVisual(0, 0, 0,0)), SetVisual {
    var views: MutableList<Tiles> = mutableListOf()
    var posx = 50.0
    val posy = 50.0
    var used = 0

    /**
     * Adds a Tiles to the selected views.
     * @param TokenView The Tile to be added.
     */
    fun addView(color: Color) {
        views.add(Tiles(posx, posy, color))
        posx+=30
    }
    /**
     * Removes a Tiles from the selected views.
     * @param view The Tile to be removed.
     */
    fun removeView(view: Tiles){
        views.remove(view)
        posx-=30
    }

    /**
     * Removes all views from selected
     */
    fun removeall(){
        for( i in (0 .. views.size-1).reversed()){
            views[i].isVisible=false
            removeView(views[i])
        }
    }

    override fun setVisual(mode: VisualMode) {
       for( i in views){
           i.setVisual(mode)
       }
    }
}