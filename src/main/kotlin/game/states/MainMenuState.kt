package game.states

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import engine.GameState
import engine.graphics.ui.GUI
import engine.graphics.ui.widgets.textButton

class MainMenuState : GameState {
    private val ui = GUI()

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.LIME)

        var returnState = this as GameState

        ui.begin()
        ui.textButton("Test level") {
            returnState = LevelGameState()
        }
        ui.textButton("Edit level") {
            returnState = LevelEditorGameState()
        }
        ui.end()

        return returnState
    }

    override fun onDestroy() {
        ui.dispose()
    }
}