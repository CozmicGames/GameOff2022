package game.states

import com.cozmicgames.Kore
import com.cozmicgames.ResizeListener
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import engine.Game
import engine.GameState
import engine.graphics.asRegion
import engine.graphics.rendergraph.RenderFunction
import engine.graphics.rendergraph.RenderGraph
import engine.graphics.rendergraph.colorRenderTargetDependency
import engine.graphics.rendergraph.functions.BlurHorizontalRenderFunction
import engine.graphics.rendergraph.functions.BlurVerticalRenderFunction
import engine.graphics.rendergraph.onRender
import engine.graphics.rendergraph.passes.ColorRenderPass
import engine.graphics.rendergraph.present.SimplePresentFunction
import engine.graphics.ui.widgets.*
import engine.scene.Scene
import engine.scene.components.TransformComponent
import engine.scene.processors.DrawableRenderProcessor
import engine.scene.processors.ParticleRenderProcessor
import engine.scene.processors.SpriteRenderProcessor
import engine.utils.FreeCameraControllerComponent
import game.GameControls
import game.components.CameraComponent
import game.components.GridComponent
import game.level.*

class LevelEditorGameState : GameState {
    private companion object {
        const val LEVEL_EDITOR_PASS_NAME = "levelEditor"
        const val LEVEL_EDITOR_BLUR_PREPASS_NAME = "levelEditorBlurPre"
        const val LEVEL_EDITOR_BLUR_PASS_NAME = "levelEditorBlur"
        const val TILETYPE_EDITOR_PASS_NAME = "tileTypeEditor"
        const val TILETYPE_EDITOR_BLUR_PREPASS_NAME = "tileTypeEditorBlurPre"
        const val TILETYPE_EDITOR_BLUR_PASS_NAME = "tileTypeEditorBlur"
        const val MENU_FROM_LEVEL_EDITOR_PASS_NAME = "menuFromLevelEditor"
        const val MENU_FROM_TILETYPE_EDITOR_PASS_NAME = "menuFromTileTypeEditor"
    }

    private val scene = Scene()
    private val editor = LevelEditor(scene)
    private val renderGraph = RenderGraph(SimplePresentFunction(LEVEL_EDITOR_PASS_NAME, 0))
    private var isMenuOpen = false
    private var isTileTypeEditorOpen = false
    private var newPresentSource: String? = null
    private val resizeListener: ResizeListener = { width, height ->
        renderGraph.resize(width, height)
    }

    override fun onCreate() {
        scene.addGameObject {
            addComponent<TransformComponent> { }
            addComponent<CameraComponent> {
                isMainCamera = true
            }
            addComponent<FreeCameraControllerComponent> { }
        }

        scene.addGameObject {
            addComponent<TransformComponent> { }
            addComponent<GridComponent> {
                tileSet = "assets/tilesets/test.tileset"
            }
        }

        scene.addSceneProcessor(SpriteRenderProcessor())
        scene.addSceneProcessor(DrawableRenderProcessor())
        scene.addSceneProcessor(ParticleRenderProcessor())

        renderGraph.onRender(LEVEL_EDITOR_PASS_NAME, ColorRenderPass(), object : RenderFunction() {
            override fun render(delta: Float) {
                Kore.graphics.clear(Color(0x726D8AFF))

                if (isMenuOpen || isTileTypeEditorOpen)
                    Game.gui.isInteractionEnabled = false

                val returnState = editor.onFrame(delta)

                if (isMenuOpen || isTileTypeEditorOpen)
                    Game.gui.isInteractionEnabled = true


                if (returnState !is LevelEditor.ReturnState.None)
                    editor.removeCameraControls()

                if (returnState is LevelEditor.ReturnState.Menu) {
                    isMenuOpen = true
                    setPresentSource(MENU_FROM_LEVEL_EDITOR_PASS_NAME)
                }

                if (returnState is LevelEditor.ReturnState.EditTileType) {
                    isTileTypeEditorOpen = true
                    setPresentSource(TILETYPE_EDITOR_PASS_NAME)
                }
            }
        })

        renderGraph.onRender(LEVEL_EDITOR_BLUR_PREPASS_NAME, ColorRenderPass(), BlurHorizontalRenderFunction(LEVEL_EDITOR_PASS_NAME, 0))
        renderGraph.onRender(LEVEL_EDITOR_BLUR_PASS_NAME, ColorRenderPass(), BlurVerticalRenderFunction(LEVEL_EDITOR_BLUR_PREPASS_NAME, 0))

        renderGraph.onRender(TILETYPE_EDITOR_PASS_NAME, ColorRenderPass(), object : RenderFunction() {
            private val colorInput = colorRenderTargetDependency(LEVEL_EDITOR_BLUR_PASS_NAME, 0)

            override fun render(delta: Float) {
                Kore.graphics.clear(Color.CLEAR)

                Game.gui.begin()
                Game.gui.transient {
                    Game.gui.image(colorInput.texture.asRegion(), pass.width.toFloat(), pass.height.toFloat())
                }

                if (isMenuOpen)
                    Game.gui.isInteractionEnabled = false

                Game.gui.group(Color(0xFFF5CCFF.toInt())) {
                    Game.gui.textButton("Back") {
                        isTileTypeEditorOpen = false
                        editor.addCameraControls()
                        setPresentSource(LEVEL_EDITOR_PASS_NAME)
                    }
                }
                Game.gui.end()

                if (isMenuOpen)
                    Game.gui.isInteractionEnabled = true
            }
        })

        renderGraph.onRender(TILETYPE_EDITOR_BLUR_PREPASS_NAME, ColorRenderPass(), BlurHorizontalRenderFunction(TILETYPE_EDITOR_PASS_NAME, 0))
        renderGraph.onRender(TILETYPE_EDITOR_BLUR_PASS_NAME, ColorRenderPass(), BlurVerticalRenderFunction(TILETYPE_EDITOR_BLUR_PREPASS_NAME, 0))

        renderGraph.onRender(MENU_FROM_LEVEL_EDITOR_PASS_NAME, ColorRenderPass(), object : RenderFunction() {
            private val colorInput = colorRenderTargetDependency(LEVEL_EDITOR_BLUR_PASS_NAME, 0)

            override fun render(delta: Float) {
                Kore.graphics.clear(Color.CLEAR)

                Game.gui.begin()
                Game.gui.transient {
                    Game.gui.image(colorInput.texture.asRegion(), pass.width.toFloat(), pass.height.toFloat())
                }
                Game.gui.end()

                drawMenu()
            }
        })

        renderGraph.onRender(MENU_FROM_TILETYPE_EDITOR_PASS_NAME, ColorRenderPass(), object : RenderFunction() {
            private val colorInput = colorRenderTargetDependency(TILETYPE_EDITOR_BLUR_PASS_NAME, 0)

            override fun render(delta: Float) {
                Kore.graphics.clear(Color.CLEAR)

                Game.gui.begin()
                Game.gui.transient {
                    Game.gui.image(colorInput.texture.asRegion(), pass.width.toFloat(), pass.height.toFloat())
                }
                Game.gui.end()

                drawMenu()
            }
        })

        Kore.addResizeListener(resizeListener)
    }

    private fun setPresentSource(name: String) {
        newPresentSource = name
    }

    private fun drawMenu() {
        Game.gui.begin()

        Game.gui.group(Color(0xFFF5CCFF.toInt())) {
            Game.gui.textButton("Resume") {
                isMenuOpen = false
                setPresentSource(
                    if (isTileTypeEditorOpen)
                        TILETYPE_EDITOR_PASS_NAME
                    else {
                        editor.addCameraControls()
                        LEVEL_EDITOR_PASS_NAME
                    }
                )
            }
            Game.gui.textButton("To Menu") {
                println("To menu") //TODO
            }
            Game.gui.textButton("Settings") {
                println("Settings") //TODO
            }
            Game.gui.textButton("Close Game") {
                Kore.stop()
            }
        }
        Game.gui.end()
    }

    override fun onFrame(delta: Float): GameState {
        newPresentSource?.let {
            renderGraph.presentRenderFunction = SimplePresentFunction(it, 0)
            newPresentSource = null
        }

        renderGraph.render(delta)

        if (GameControls.openMenuFromLevel.isTriggered) {
            isMenuOpen = !isMenuOpen

            if (isMenuOpen)
                setPresentSource(if (isTileTypeEditorOpen) MENU_FROM_TILETYPE_EDITOR_PASS_NAME else MENU_FROM_LEVEL_EDITOR_PASS_NAME)
            else
                setPresentSource(if (isTileTypeEditorOpen) TILETYPE_EDITOR_PASS_NAME else LEVEL_EDITOR_PASS_NAME)
        }

        return this
    }

    override fun onDestroy() {
        scene.dispose()
        renderGraph.dispose()
        Kore.removeResizeListener(resizeListener)
    }
}