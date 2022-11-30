package engine

import com.cozmicgames.Application
import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.Primitive
import com.cozmicgames.graphics.averageFramesPerSecond
import com.cozmicgames.input
import com.cozmicgames.input.Keys
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.injector
import com.cozmicgames.utils.maths.OrthographicCamera
import engine.audio.SoundManager
import engine.graphics.*
import engine.graphics.ui.GUI
import engine.graphics.ui.widgets.label
import engine.graphics.ui.widgets.separator
import engine.input.ControlManager
import engine.materials.MaterialManager
import engine.physics.Physics
import engine.utils.Rumble
import game.assets.AssetManager
import game.level.TileSetManager
import game.states.LoadingGameState

object Game : Application {
    val sounds by Kore.context.injector(true) { SoundManager() }
    val textures by Kore.context.injector(true) { TextureManager() }
    val fonts by Kore.context.injector(true) { FontManager() }
    val shaders by Kore.context.injector(true) { ShaderManager() }
    val materials by Kore.context.injector(true) { MaterialManager() }
    val tileSets by Kore.context.injector(true) { TileSetManager() }
    val assets by Kore.context.injector(true) { AssetManager() }

    val controls by Kore.context.injector(true) { ControlManager() }
    val graphics2d by Kore.context.injector(true) { Graphics2D() }
    val renderer by Kore.context.injector(true) { RenderManager() }

    val physics by Kore.context.injector(true) { Physics() }
    val rumble by Kore.context.injector(true) { Rumble() }
    val camera by Kore.context.injector(true) { OrthographicCamera(Kore.graphics.width, Kore.graphics.height) }

    private lateinit var currentState: GameState

    private lateinit var gui: GUI
    private var isPaused = false
    private var showStatistics = false

    override fun onCreate() {
        gui = GUI()

        camera.position.setZero()
        camera.update()

        currentState = LoadingGameState()
        currentState.onCreate()
    }

    override fun onFrame(delta: Float) {
        if (isPaused)
            return

        val newState = currentState.onFrame(delta)

        if (currentState != newState) {
            currentState.onDestroy()
            newState.onCreate()
            currentState = newState
        }

        if (Kore.input.isKeyJustDown(Keys.KEY_F1))
            showStatistics = !showStatistics

        if (showStatistics) {
            gui.begin()
            with(Kore.graphics.statistics) {
                gui.group(Color(0.3f, 0.3f, 0.3f, 0.7f)) {
                    gui.label("Statistics", null)
                    gui.label("$averageFramesPerSecond FPS (${String.format("%.4f", averageFrameTime)} ms per frame)", null)
                    gui.separator()
                    gui.sameLine {
                        gui.group {
                            gui.label("# of draw calls:", null)
                            gui.label("# of compute dispatches:", null)
                            gui.label("# of buffers:", null)
                            gui.label("# of textures:", null)
                            gui.label("# of frame buffers:", null)
                            gui.label("# of pipelines:", null)
                        }
                        gui.group {
                            gui.label(numDrawCalls.toString(), null)
                            gui.label(numComputeDispatches.toString(), null)
                            gui.label(numBuffers.toString(), null)
                            gui.label(numTextures.toString(), null)
                            gui.label(numFramebuffers.toString(), null)
                            gui.label(numPipelines.toString(), null)
                        }
                    }
                    gui.separator()
                    gui.label("Number of rendered primitives", null)
                    gui.sameLine {
                        gui.group {
                            Primitive.values().forEach {
                                gui.label("$it:", null)
                            }
                        }
                        gui.group {
                            Primitive.values().forEach {
                                gui.label(getNumberOfRenderedPrimitives(it).toString(), null)
                            }
                        }
                    }
                }
            }
            gui.end()
        }
    }

    override fun onPause() {
        isPaused = true
    }

    override fun onResume() {
        isPaused = false
    }

    override fun onResize(width: Int, height: Int) {
        camera.width = width
        camera.height = height
    }

    override fun onDispose() {
        currentState.onDestroy()
        gui.dispose()
    }
}
