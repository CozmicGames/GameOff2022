package engine

import com.cozmicgames.Application
import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.injector
import com.cozmicgames.utils.maths.OrthographicCamera
import engine.audio.SoundManager
import engine.graphics.*
import engine.input.ControlManager
import engine.physics.Physics
import engine.utils.Rumble
import game.InitialGameState

object Game : Application {
    val sounds by Kore.context.injector(true) { SoundManager() }
    val textures by Kore.context.injector(true) { TextureManager() }
    val shaders by Kore.context.injector(true) { ShaderManager() }

    val controls by Kore.context.injector(true) { ControlManager() }
    val graphics2d by Kore.context.injector(true) { Graphics2D() }
    val renderer by Kore.context.injector(true) { RenderManager() }



    val physics by Kore.context.injector(true) { Physics() }
    val rumble by Kore.context.injector(true) { Rumble() }
    val camera by Kore.context.injector(true) { OrthographicCamera(Kore.graphics.width, Kore.graphics.height) }

    private lateinit var currentState: GameState

    override fun onCreate() {
        camera.position.setZero()
        camera.update()

        currentState = InitialGameState()
        currentState.onCreate()
    }

    override fun onFrame(delta: Float) {
        val newState = currentState.onFrame(delta)

        if (currentState != newState) {
            currentState.onDestroy()
            newState.onCreate()
            currentState = newState
        }
    }

    override fun onPause() {

    }

    override fun onResume() {

    }

    override fun onResize(width: Int, height: Int) {
        camera.width = width
        camera.height = height
    }

    override fun onDispose() {
        currentState.onDestroy()
    }
}
