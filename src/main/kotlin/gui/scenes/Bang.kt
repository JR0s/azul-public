package gui.scenes

import gui.Sounds
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.lang.Thread.sleep
import javax.swing.JFrame
import kotlin.concurrent.thread


class Bang {




    val bang = JFrame("Bang!")

    init {

        Sounds().playSound(Sounds.SoundKey.BANG)


        bang.setSize(1920,1080)
        bang.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        thread(true) {
            sleep(1100)
            Sounds().playSound(Sounds.SoundKey.BANGTOLOUD)
        bang.isVisible = true
            sleep(2000)
        bang.isVisible = false }
    }


}