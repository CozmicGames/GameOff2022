package game.states

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.writeString
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.color
import com.cozmicgames.utils.float
import com.cozmicgames.utils.string
import engine.GameState
import engine.graphics.ui.GUI
import engine.graphics.ui.widgets.textButton
import engine.materials.Material

class MainMenuState : GameState {
    private val ui = GUI()

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.LIME)

        var returnState = this as GameState

        ui.begin()
        ui.textButton("Test level") {
            returnState = LevelGameState()
        }
        ui.end()

        return returnState
    }

    override fun onDestroy() {
        ui.dispose()
    }
}