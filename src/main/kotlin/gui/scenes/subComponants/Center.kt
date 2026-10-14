package gui.scenes.subComponants

import gui.VisualMode
import gui.scenes.builder.CenterValues
import gui.scenes.builder.SetVisual
import gui.scenes.builder.center

import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.Pane

/** Center.kt
 * Represents the center of the game board.
 * It extends Pane to allow for layout management of components.
 * This is so the camera can focus on the center of the game board.
 */
class Center(posX: Number = 0, posY: Number = 0, width: Number = 0, height: Number = 0, val ammount: Int = 5) :
    Pane<Pane<ComponentView>>(
    posX ,
    posY ,
    width ,
    height
), CenterValues, SetVisual {



    var middle: Middle = Middle(
        posX = super.width/2.0,
        posY = super.height/2.0
    )
    var factorys: Factorys = Factorys(
        posX = 0,
        posY = 0,
        width = super.width,
        height = super.height,
        ammount = ammount
    )


    init {
        val middleCenterCords =  middle.center(
            posX = super.width/2.0,
            posY = super.height/2.0,
        )
        middle.reposition(
            posX = middleCenterCords.first,
            posY = middleCenterCords.second
        )
        add(factorys)
        add(middle)
    }

    /**
     * Creates a new Factorys component at the specified position with the given amount.
     * It removes the existing Factorys component before creating a new one.
     *
     * @param posX The x-coordinate for the new Factorys component.
     * @param posY The y-coordinate for the new Factorys component.
     * @param ammount The number of factories to create.
     */

    fun createFactorys(posX:Number, posY: Number, ammount: Int) {

        remove(factorys)

        factorys = Factorys(
            posX = posX,
            posY = posY,
            width = width,
            height = height,
            ammount = ammount
        )
        val factorysCenterCords = factorys.center(
            posX = super.width/2.0,
            posY = super.height/2.0,
        )
        factorys.reposition(
            posX = factorysCenterCords.first,
            posY = factorysCenterCords.second
        )
        add(factorys)
    }

    /**
     * add a view to middle
     */
    fun addit(view: Tiles){
        remove(middle)
        middle.addView(view)
        add(middle)
    }

    /**
     * helps to create a new middle
     */
    fun setmiddle(mid: entity.Middle ){
        remove(middle)
        for (i in mid.tiles.indices){
            middle.addit(mid.tiles[i])
        }
        add(middle)
    }

    override fun setVisual(mode: VisualMode) {
        middle.setVisual(mode)
        factorys.setVisual(mode)
    }
}