package game.states

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import engine.GameState

class MainMenuState:GameState {
    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.LIME)

        return this
    }
}