package gui.scenes

import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual



/**
 * MenuScene that shows the results
 */
class GameOverScene(val rootService: RootService) : MenuScene(600, 400), Refreshable {

    var winners = 1
    val headlineLabel = Label(
        width = 300, height = 50, posX = 150, posY = 30,
        text = "GAME OVER",
        font = Font(size = 24)
    )
    private val playerScore1 = Label(
        width = 100, height = 50, posX = 50, posY = 280,
        text = "Player2",
    ).apply{isVisible = false}
    private val score1 = Label(
        width = 100, height = 50, posX = 50, posY = 305,
        text = "",
    ).apply{isVisible = false}
    private val playerScore2 = Label(
        width = 100, height = 50, posX = 250, posY = 280,
        text = "Player3",
    ).apply{isVisible = false}
    private val score2 = Label(
        width = 100, height = 50, posX = 250, posY = 305,
        text = "",
    ).apply{isVisible = false}
    private val playerScore3 = Label(
        width = 100, height = 50, posX = 450, posY = 280,
        text = "Player4",
    ).apply{isVisible = false}
    private val score3 = Label(
        width = 100, height = 50, posX = 450, posY = 305,
        text = "",
    ).apply{isVisible = false}
    val restartButton = Button(
        width = 150, height = 35,
        posX = 225, posY = 350,
        text = "Restart"
    )

    /**
     * initialise the components
     */
    init {
        background = ColorVisual(0, 0, 0, 0)
        opacity = .5
        addComponents(
            playerScore1, score1, playerScore2, score2,
            headlineLabel, restartButton, playerScore3, score3)
    }
    /**
     * determine winner and set labels
     */
    private fun setLabels(){
        val game = rootService.currentGame
        checkNotNull(game){"Game should be running"}
        val results : List<Triple<String, Int, Int>> = game.azulGameState.players.map{ player ->
            val filledRowsCount = player.board.wall.count { row ->
                row.all { it.filled }
            }
            Triple(player.name, player.score,
                filledRowsCount)
        }
        check(results.size >=2){"Invalid number of players"}
        val presort = results.sortedByDescending { it.third }
        println(presort[0].third)
        val sorted = presort.sortedByDescending { it.second }
        setWinners(sorted)

        if(sorted.size-winners > 0){
            playerScore1.text = sorted[winners].first
            score1.text = sorted[winners].second.toString()
        }

        if(sorted.size-winners > 1){
            playerScore2.text = sorted[1+winners].first
            score2.text = sorted[1+winners].second.toString()
        }

        if(results.size-winners > 2){
            playerScore3.text = sorted[2+winners].first
            score3.text = sorted[2+winners].second.toString()
        }
    }

    /**
     * display results
     */
    private fun showLabels(){
        val game = rootService.currentGame
        checkNotNull(game){"Game should be running"}
        when(game.azulGameState.players.size-winners){
            1 -> {
                playerScore1.posX = 250.0
                playerScore1.isVisible = true
                score1.posX = 250.0
                score1.isVisible = true
            }
            2 -> {
                playerScore1.posX = 150.0
                playerScore1.isVisible = true
                score1.posX = 150.0
                score1.isVisible = true

                playerScore2.posX = 350.0
                playerScore2.isVisible = true
                score2.posX = 350.0
                score2.isVisible = true
            }
            3 -> {
                playerScore1.isVisible = true
                score1.isVisible = true

                playerScore2.isVisible = true
                score2.isVisible = true

                playerScore3.isVisible = true
                score3.isVisible = true
            }
        }
    }

    /**
     * displays Winners and crowns
     */
    private fun setWinners(sorted:List<Triple<String,Int,Int>>){
        if (sorted[0].second==sorted[1].second&&sorted[0].third==sorted[1].third){
            winners++
            if (sorted[1].second==sorted[2].second&&sorted[1].third==sorted[2].third) {
                winners++
                if (sorted[2].second==sorted[3].second&&sorted[2].third==sorted[3].third){
                    winners++
                }
            }
        }
        val distanz = 500/(1+winners)
        println(winners)
        for (i in 1.. winners){
            val winnerName = Label(
                width = 100, height = 30, posX = distanz*(i), posY = 200,
                text = sorted[i-1].first,
                font = Font(size = 20)
            )
            val winnerScore = Label(
                width = 100, height = 50, posX = distanz*(i), posY = 220,
                text = sorted[i-1].second.toString(),
                font = Font(size = 20)
            )
            val krone = Label(distanz*(i),115,100,100).apply {
                visual= ImageVisual("normal/Krone.png")
            }
            addComponents(winnerName, winnerScore,krone)
        }
    }

    override fun refreshAfterGameOver(){
        this.setLabels()
        this.showLabels()
    }
}