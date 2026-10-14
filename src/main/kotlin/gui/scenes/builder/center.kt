package gui.scenes.builder


/**
    * Provides a function to calculate the center of a component
    * so that the componant can be moved as if the cordinates that are given are the center of the componant.
    * Not the upper left corner.
    * @return Pair<Double, Double> representing the center coordinates of the component.
 */
fun <T> T.center(posX: Double, posY: Double): Pair<Double, Double> where T : Any, T: CenterValues {

    return Pair(posX - (this.width / 2.0),
                posY - (this.height / 2.0))
}

/**
 * Interface to define the width and height of a component.
 * Used to calculate the center of the component.
 * This is just to enshure that the component has a width and height
 */
interface CenterValues {
    val width: Double
    val height: Double
}
