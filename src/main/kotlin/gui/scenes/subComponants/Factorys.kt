package gui.scenes.subComponants

import entity.Color
import gui.VisualMode
import gui.scenes.builder.CenterValues
import gui.scenes.builder.SetVisual
import gui.scenes.builder.center
import gui.scenes.builder.circle
import service.Refreshable
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.layoutviews.Pane


/**
 * Factorys.kt
 * Represents a collection of factory components in the Azul game.
 * It extends Pane to allow for layout management of components.
 */
class Factorys(posX : Number = 0, posY : Number = 0, width: Number = 1920, height: Number = 1080, ammount: Int = 5) :
    CenterValues,
    Refreshable,
    Pane<ComponentView>(posX, posY, width, height ), SetVisual {

    val factoryList: MutableList<Factory> = mutableListOf()

    init {

        circle(ammount,
            175.0,
            (super.width / 2.0),
            (super.height / 2.0),
        ).forEach {
            val factory = Factory(
                it.x, it.y,
                array =  arrayOf(Color.INVISIBLE, Color.INVISIBLE,
                    Color.INVISIBLE, Color.INVISIBLE)
            )
            factory.rotate(it.rotation)
            val factoryCenter = factory.center(
                posX = it.x,
                posY = it.y
            )
            factory.reposition(
                posX = factoryCenter.first,
                posY = factoryCenter.second
            )
            factoryList.add(factory)
            add(factory)


         }
    }

    override fun setVisual(mode: VisualMode) {
        for(i in factoryList){
            i.setVisual(mode)
        }
    }
    }
