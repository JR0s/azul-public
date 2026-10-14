package gui.scenes.subComponants

import entity.Color
import gui.VisualMode
import gui.scenes.builder.CenterValues
import gui.scenes.builder.SetVisual
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.visual.ColorVisual
import kotlin.random.Random

/**
    * Middle.kt
    * displays the middle of the game board
 */
class Middle(posX: Double = 200.0, posY: Double = 200.0) : CenterValues, Pane<ComponentView>(
    posX = posX,
    posY = posY,
    width = 100,
    height = 100,
    visual = ColorVisual(0, 0, 0, 0) // Transparent background
), SetVisual {
    var views: MutableList<Tiles> = mutableListOf()

    /**
     * adds a new Tile to views with the given color
     */
    fun addit(color: Color){
        when(color){
            Color.YELLOW ->{
                addView(Tiles(40.0 + Random.nextDouble(-20.0,20.0),
                    115.0 + Random.nextDouble(-30.0,30.0),Color.YELLOW))
            }
            Color.RED -> {
                addView(Tiles(-31.3 + Random.nextDouble(-20.0,20.0),
                    63.2 + Random.nextDouble(-30.0,30.0),Color.RED))
            }
            Color.BLUE -> {
                addView(Tiles(-3.1 + Random.nextDouble(-20.0,20.0),
                    -20.67 + Random.nextDouble(-30.0,30.0),Color.BLUE))
            }
            Color.WHITE -> {
                addView(Tiles(84.08 + Random.nextDouble(-20.0,20.0),
                    -20.67 + Random.nextDouble(-30.0,30.0),Color.WHITE))
            }
            Color.BLACK -> {
                addView(Tiles(111.3 + Random.nextDouble(-20.0,20.0),
                    63.2 + Random.nextDouble(-30.0,30.0),Color.BLACK))
            }
            Color.START_MARKER -> {
                addView(Tiles(40.0, 40.0,Color.START_MARKER))
            }
            else -> {
                IllegalStateException("keine Farbe")
            }
        }
    }

    /**
     * adds a given view to middle
     */
    fun addView(view: Tiles) {
        views.add(view)
        add(view)
        view.toFront()
    }

    /**
     * removes all views from middle
     */
    fun removeall(){

        for(i in( 0..views.size-1).reversed()){
            views[i].isVisible=false
            views.remove(views[i])
        }
    }

    override fun setVisual(mode: VisualMode) {
        for(i in views){
            i.setVisual(mode)
        }
    }
}


//class Middle(): TokenView(800,600,100,100, visual = ColorVisual(0, 0, 0,0)){
//    var Views: MutableList<Tiles> = mutableListOf()
//
//    fun addView(view: Tiles) {
//        Views.add(view)
//    }
//}


