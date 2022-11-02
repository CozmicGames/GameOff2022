package game.states

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.files
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.extension
import com.cozmicgames.files.isDirectory
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.durationOf
import engine.Game
import engine.GameState
import engine.graphics.ui.GUI
import engine.graphics.ui.widgets.label
import game.Constants
import kotlin.math.min

class LoadingGameState : GameState {
    private val loadingTasks = arrayListOf<() -> Unit>()
    private var totalTasks = 0
    private var loadedTasks = 0

    private val ui = GUI()

    override fun onCreate() {
        fun searchDirectory(directoryFile: FileHandle) {
            directoryFile.list {
                val file = directoryFile.child(it)
                if (file.isDirectory)
                    searchDirectory(file)
                else {
                    if (file.extension in Kore.graphics.supportedImageFormats)
                        loadingTasks += { Game.textures.add(file) }

                    if (file.extension in Kore.graphics.supportedFontFormats)
                        loadingTasks += { Game.fonts.add(file) }

                    if (file.extension in Kore.audio.supportedSoundFormats)
                        loadingTasks += { Game.sounds.add(file) }

                    if (file.extension.lowercase() == "shader")
                        loadingTasks += { Game.shaders.add(file) }
                }
            }
        }

        searchDirectory(Kore.files.asset("assets"))

        Game.textures.add(Kore.files.asset("icons/icon.png"))

        totalTasks = loadingTasks.size
    }

    override fun onFrame(delta: Float): GameState {
        var usedTime = 0.0

        while (loadingTasks.isNotEmpty() && usedTime < 1.0f / delta) {
            usedTime += durationOf {
                loadingTasks.removeFirst()()
                loadedTasks++
            }
        }

        Kore.graphics.clear(Color.LIME)

        val progressWidth = Kore.graphics.width * 0.75f
        val progressHeight = 24.0f

        val progressX = Kore.graphics.width * 0.5f
        val progressY = Kore.graphics.height * 0.25f

        val iconSize = min(Kore.graphics.width, Kore.graphics.height) * 0.5f

        val iconX = (Kore.graphics.width - iconSize) * 0.5f
        val iconY = (Kore.graphics.height - iconSize) * 0.66f

        val progress = loadedTasks.toFloat() / totalTasks

        Game.graphics2d.render {
            it.draw(Game.textures[Kore.files.asset("icons/icon.png")]!!, iconX, iconY, iconSize, iconSize)
            it.drawRect(progressX, progressY, progressWidth, progressHeight, Color.DARK_GRAY)
            it.drawRect(progressX, progressY, progressWidth * progress, progressHeight, Color.RED)
        }

        ui.begin()
        ui.label(Constants.versionString, Color.CLEAR)
        ui.end()

        return if (loadedTasks == totalTasks) MainMenuState() else this
    }
}
