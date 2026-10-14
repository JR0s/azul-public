package gui


import java.io.BufferedInputStream
import java.io.InputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.UnsupportedAudioFileException
import javax.sound.sampled.LineUnavailableException
import java.io.IOException
import kotlin.concurrent.thread

class Sounds {
    /**
     * Plays an audio file from resources/brainRot/sounds in a separate thread.
     * Supports WAV files. For MP3, JavaFX Media is recommended (not implemented here).
     * @param filename The filename in resources/brainRot/sounds (e.g., "Mc.wav")
     */
    fun playSound(key : SoundKey) {
        val filename = SoundKey.getSoundPath(key)
        thread(true) {
            println("Starting audio thread for: $filename")
            val resourcePath = "/brainRot/sounds/$filename"
            val audioStream: AudioInputStream? = try {
                val inputStream: InputStream? = javaClass.getResourceAsStream(resourcePath)
                if (inputStream == null) {
                    println("Audio file not found: $resourcePath")
                    return@thread
                }
                AudioSystem.getAudioInputStream(BufferedInputStream(inputStream))
            } catch (_: UnsupportedAudioFileException) {
                println("Unsupported audio file: $filename")
                return@thread
            } catch (_: IOException) {
                println("Error loading audio file: $filename")
                return@thread
            }
            try {
                val clip: Clip = AudioSystem.getClip()
                clip.open(audioStream)
                clip.start()
                // Wait for playback to finish

                thread(true) {
                    Thread.sleep(clip.microsecondLength / 1000)
                    clip.close() }

            } catch (_: LineUnavailableException) {
                println("Audio line unavailable for: $filename")
            } catch (_: IOException) {
                println("Error playing audio file: $filename")
            }
        }
    }
  enum class SoundKey {
        START_ROUND,
        TILE_FLOORLINE,
        TILE_TAKEN,
        TILE_PATTERNLINE,
        GAME_OVERSOUND,
        ROUNDSTARTSOUND,
        NAME2H,
        STARTGAMESOUND,
        ONSAVE,
        BANGTOLOUD,
        BANG;

      companion object{
          private val map = mapOf(
                START_ROUND to "aShit.wav",
                TILE_FLOORLINE to "bruuuuu.wav",
                TILE_TAKEN to "Mc.wav",
                TILE_PATTERNLINE to "rizz.wav",
                GAME_OVERSOUND to "",
                ROUNDSTARTSOUND to "aShit.wav",//A shit here we go again
                NAME2H to "",
                STARTGAMESOUND to "startGameSound.wav",
                ONSAVE to "saveSound.wav",
                BANG to "bang.wav",
              BANGTOLOUD to "bangtl.wav",

          )

        fun getSoundPath(key:  SoundKey): String {
            return map[key] ?: ""
        }
      }
    }


}