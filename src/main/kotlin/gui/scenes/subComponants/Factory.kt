package gui.scenes.subComponants

import entity.Color
import gui.VisualMode
import gui.scenes.builder.CenterValues
import gui.scenes.builder.SetVisual
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.visual.ImageVisual
import kotlin.random.Random

/**
 * Factory.kt
 * Represents a factory in the Azul game.
 * It contains four tiles, each with a random rotation.
 */
class Factory(posX:Double, posY:Double, array: Array<Color>) : CenterValues,
    Pane<TokenView>(posX, posY, width = 50, height = 50  ), SetVisual {

    val back: TokenView = TokenView(0,
        0,
        80,
        80,
        gui.scenes.builder.VisKey.getVisual(gui.scenes.builder.VisKey.FACTORY_WHITE, gui.AzulApplication.mode)).apply {
        scale(2)
    }
    var tile1: Tiles = Tiles( 10.0,  10.0, array[0]).apply {
        rotate(Random.nextInt(0, 359))
    }
    var tile2: Tiles = Tiles(10.0,  40.0, array[1]).apply {
        rotate(Random.nextInt(0, 359))
    }
    var tile3: Tiles = Tiles( 40.0,  10.0, array[2]).apply {
        rotate(Random.nextInt(0, 359))
    }
    var tile4: Tiles = Tiles( 40.0,  40.0, array[3]).apply {
        rotate(Random.nextInt(0, 359))
    }
    var tiles = mutableListOf(tile1, tile2, tile3, tile4)


    init {
        addAll(back,
            tile1,
            tile2,
            tile3,
            tile4
        )
    }
    override fun setVisual(mode: VisualMode) {
     for (i in tiles){
         i.setVisual(mode)
     }

    }

}