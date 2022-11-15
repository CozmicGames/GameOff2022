package game.states

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input
import com.cozmicgames.input.Keys
import com.cozmicgames.input.MouseButtons
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.collections.Array2D
import com.cozmicgames.utils.maths.*
import engine.Game
import engine.GameState
import engine.graphics.asRegion
import engine.graphics.drawPathStroke
import engine.graphics.drawRect
import engine.graphics.ui.GUI
import engine.graphics.ui.widgets.imageButton
import engine.graphics.ui.widgets.label
import engine.graphics.ui.widgets.selectableImage
import engine.graphics.ui.widgets.textButton
import engine.scene.Scene
import engine.scene.components.TransformComponent
import engine.scene.processors.DrawableRenderProcessor
import engine.scene.processors.ParticleRenderProcessor
import engine.scene.processors.SpriteRenderProcessor
import engine.utils.FreeCameraControllerComponent
import game.GameControls
import game.components.CameraComponent
import game.components.GridCellComponent
import game.components.GridComponent
import game.states.editor.*
import kotlin.math.ceil
import kotlin.math.floor

class LevelEditorGameState : GameState {
    private companion object {
        const val TOOL_IMAGE_SIZE = 32.0f
        val PANEL_BACKGROUND_COLOR = Color(0x575B5BFF)
    }

    private enum class ToolType(val texture: String) {
        PENCIL("assets/images/pencil_tool.png"),
        DELETE("assets/images/delete_tool.png"),
        SELECT("assets/images/select_tool.png"),
        PICK("assets/images/pick_tool.png"),
        FILL("assets/images/fill_tool.png"),
        UNDO("assets/images/undo_tool.png"),
        REDO("assets/images/redo_tool.png"),
        COPY("assets/images/copy_tool.png"),
        PASTE("assets/images/paste_tool.png"),
        SETTINGS("assets/images/settings.png")
    }

    private var currentTool = ToolType.PENCIL
    private var currentType = "test"

    private val cursorPosition = Vector2()
        get() {
            field.x = Kore.input.x.toFloat()
            field.y = Kore.input.y.toFloat()
            return field
        }

    private var copiedRegion: Array2D<String?>? = null
    private var selectionRegion: GridRegion? = null

    private val scene = Scene()
    private val gridObject = scene.addGameObject {
        addComponent<TransformComponent> {}
        addComponent<GridComponent> {
            tileSet = "assets/tilesets/test.tileset"
        }
    }
    private val editor = LevelEditor()

    private val ui = GUI()
    private var isMenuOpen = false
        set(value) {
            field = value
            if (value)
                removeCameraControls()
            else
                addCameraControls()
        }

    override fun onCreate() {
        scene.addGameObject {
            addComponent<TransformComponent> { }
            addComponent<CameraComponent> {
                isMainCamera = true
            }
            addComponent<FreeCameraControllerComponent> { }
        }

        scene.addSceneProcessor(SpriteRenderProcessor())
        scene.addSceneProcessor(DrawableRenderProcessor())
        scene.addSceneProcessor(ParticleRenderProcessor())

        addCameraControls()
    }

    private fun addCameraControls() {
        Game.controls.add("freecamera_move").also {
            it.addMouseButton(MouseButtons.MIDDLE)
        }

        Game.controls.add("freecamera_move_x").also {
            it.setDeltaX()
        }

        Game.controls.add("freecamera_move_y").also {
            it.setDeltaY()
        }

        Game.controls.add("freecamera_zoom").also {
            it.setScrollY()
        }
    }

    private fun removeCameraControls() {
        Game.controls.remove("freecamera_move")
        Game.controls.remove("freecamera_move_x")
        Game.controls.remove("freecamera_move_y")
        Game.controls.remove("freecamera_zoom")
    }

    private fun copySelection() {
        selectionRegion?.let {
            copiedRegion = it.getTiles()
        }
    }

    private fun pasteSelection() {
        selectionRegion?.let {
            copiedRegion?.let { source ->
                editor.setTiles(it, source)
            }
        }
    }

    private fun deleteSelection() {
        selectionRegion?.let {
            it.setTiles { _, _ -> null }
        }
    }

    private fun drawBackground(grid: GridComponent, camera: OrthographicCamera) {
        val backgroundTexture = Game.textures["assets/images/grid_background_8x8.png"] ?: Game.graphics2d.missingTexture.asRegion()

        val backgroundTileWidth = 8 * grid.cellSize
        val backgroundTileHeight = 8 * grid.cellSize

        val numBackgroundTilesX = ceil(camera.rectangle.width / backgroundTileWidth).toInt() + 1
        val numBackgroundTilesY = ceil(camera.rectangle.height / backgroundTileHeight).toInt() + 1

        var backgroundTileX = floor((camera.position.x - camera.rectangle.width * 0.5f) / backgroundTileWidth) * backgroundTileWidth

        repeat(numBackgroundTilesX) { x ->
            var backgroundTileY = floor((camera.position.y - camera.rectangle.height * 0.5f) / backgroundTileHeight) * backgroundTileHeight

            repeat(numBackgroundTilesY) { y ->
                Game.renderer.submit(grid.layer - 1, backgroundTexture.texture, "default", false, false) {
                    it.drawRect(backgroundTileX, backgroundTileY, backgroundTileWidth, backgroundTileHeight, color = Color.LIGHT_GRAY, u0 = backgroundTexture.u0, v0 = backgroundTexture.v0, u1 = backgroundTexture.u1, v1 = backgroundTexture.v1)
                }

                backgroundTileY += backgroundTileHeight
            }

            backgroundTileX += backgroundTileWidth
        }
    }

    fun drawCurrentTool(grid: GridComponent, camera: OrthographicCamera) {
        val (worldX, worldY, _) = camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())

        val tileX = floor(worldX / grid.cellSize).toInt()
        val tileY = floor(worldY / grid.cellSize).toInt()

        val selectionRegion = this.selectionRegion

        if (selectionRegion != null) {
            Game.renderer.submit(grid.layer - 1, Game.graphics2d.blankTexture, "default", false, false) {
                val x = selectionRegion.minX * grid.cellSize
                val y = selectionRegion.minY * grid.cellSize
                val width = selectionRegion.width * grid.cellSize
                val height = selectionRegion.height * grid.cellSize

                it.drawPathStroke(it.path {
                    rect(x, y, width, height)
                }, 2.0f, true, Color.WHITE)
            }
        }

        if (cursorPosition in Rectangle(Kore.graphics.width - TOOL_IMAGE_SIZE, Kore.graphics.height - ToolType.values().size * TOOL_IMAGE_SIZE, TOOL_IMAGE_SIZE, ToolType.values().size * TOOL_IMAGE_SIZE))
            return

        when (currentTool) {
            ToolType.PENCIL -> {
                val currentTypeTexture = Game.materials[Game.tileSets[grid.tileSet][currentType].material]?.colorTexturePath?.let {
                    Game.textures[it]
                } ?: Game.graphics2d.missingTexture.asRegion()

                Game.renderer.submit(grid.layer - 1, currentTypeTexture.texture, "default", false, false) {
                    it.drawRect(tileX * grid.cellSize, tileY * grid.cellSize, grid.cellSize, grid.cellSize, Color(1.0f, 1.0f, 1.0f, 0.5f), u0 = currentTypeTexture.u0, v0 = currentTypeTexture.v0, u1 = currentTypeTexture.u1, v1 = currentTypeTexture.v1)
                }

                if (Kore.input.isButtonDown(MouseButtons.LEFT) && grid.getCellObject(tileX, tileY)?.getComponent<GridCellComponent>()?.tileType != currentType)
                    editor.setTile(grid, tileX, tileY, currentType)

                if (Kore.input.isButtonDown(MouseButtons.RIGHT) && grid.getCellObject(tileX, tileY)?.getComponent<GridCellComponent>()?.tileType != null)
                    editor.setTile(grid, tileX, tileY, null)
            }
            ToolType.DELETE -> {
                if ((Kore.input.isButtonDown(MouseButtons.LEFT) || Kore.input.isButtonDown(MouseButtons.RIGHT)) && grid.getCellObject(tileX, tileY)?.getComponent<GridCellComponent>()?.tileType != null)
                    editor.setTile(grid, tileX, tileY, null)
            }
            ToolType.SELECT -> {
                if (Kore.input.isButtonJustDown(MouseButtons.RIGHT))
                    this.selectionRegion = null

                if (selectionRegion == null) {
                    Game.renderer.submit(grid.layer - 1, Game.graphics2d.blankTexture, "default", false, false) {
                        it.drawPathStroke(it.path {
                            rect(tileX * grid.cellSize, tileY * grid.cellSize, grid.cellSize, grid.cellSize)
                        }, 2.0f, true, Color(1.0f, 1.0f, 1.0f, 0.5f))
                    }
                }

                if (Kore.input.isButtonJustDown(MouseButtons.LEFT))
                    this.selectionRegion = GridRegion(grid, tileX, tileY, tileX, tileY)

                if (Kore.input.isButtonDown(MouseButtons.LEFT)) {
                    this.selectionRegion?.x1 = tileX
                    this.selectionRegion?.y1 = tileY
                }
            }
            ToolType.PICK -> {
                val type = grid.getCellObject(tileX, tileY)?.getComponent<GridCellComponent>()?.tileType
                if (type != null)
                    currentType = type
            }
            else -> {}
        }
    }

    fun drawToolSelection() {
        ui.begin()
        ui.setLastElement(ui.absolute(Kore.graphics.width - TOOL_IMAGE_SIZE, 0.0f))
        ui.group(PANEL_BACKGROUND_COLOR) {
            ToolType.values().forEach {
                val texture = Game.textures[it.texture] ?: Game.graphics2d.missingTexture.asRegion()
                when (it) {
                    ToolType.FILL -> ui.imageButton(texture, TOOL_IMAGE_SIZE, TOOL_IMAGE_SIZE) {
                        selectionRegion?.let {
                            editor.setTiles(it, currentType)
                        }
                    }
                    ToolType.UNDO -> ui.imageButton(texture, TOOL_IMAGE_SIZE, TOOL_IMAGE_SIZE) {
                        editor.undo()
                    }
                    ToolType.REDO -> ui.imageButton(texture, TOOL_IMAGE_SIZE, TOOL_IMAGE_SIZE) {
                        editor.redo()
                    }
                    ToolType.COPY -> ui.imageButton(texture, TOOL_IMAGE_SIZE, TOOL_IMAGE_SIZE) {
                        copySelection()
                    }
                    ToolType.PASTE -> ui.imageButton(texture, TOOL_IMAGE_SIZE, TOOL_IMAGE_SIZE) {
                        pasteSelection()
                    }
                    ToolType.SETTINGS -> ui.imageButton(texture, TOOL_IMAGE_SIZE, TOOL_IMAGE_SIZE) {
                        isMenuOpen = true
                    }
                    else -> ui.selectableImage(texture, TOOL_IMAGE_SIZE, TOOL_IMAGE_SIZE, currentTool == it) {
                        selectionRegion = null
                        currentTool = it
                    }
                }
            }
        }
        ui.end()
    }

    fun drawCoordinateInfoLine(grid: GridComponent, camera: OrthographicCamera) {
        ui.begin()
        ui.setLastElement(ui.absolute(0.0f, Kore.graphics.height - ui.skin.elementSize))
        ui.group(PANEL_BACKGROUND_COLOR) {
            ui.sameLine {
                val (worldX, worldY, _) = camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())

                val tileX = floor(worldX / grid.cellSize).toInt()
                val tileY = floor(worldY / grid.cellSize).toInt()

                ui.label("Cursor: $tileX, $tileY", null)

                selectionRegion?.let {
                    ui.spacing()
                    ui.label("Selected: ${it.width} x ${it.height}", null)
                }

                copiedRegion?.let {
                    ui.spacing()
                    ui.label("Copied: ${it.width} x ${it.height}", null)
                }
            }
        }
        ui.end()
    }

    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color(0x726D8AFF))

        scene.update(delta)

        var mainCameraComponent: CameraComponent? = null

        for (gameObject in scene.activeGameObjects) {
            val cameraComponent = gameObject.getComponent<CameraComponent>() ?: continue

            if (cameraComponent.isMainCamera) {
                mainCameraComponent = cameraComponent
                break
            }
        }

        mainCameraComponent ?: return this
        val grid = gridObject.getComponent<GridComponent>() ?: return this
        val mainCamera = mainCameraComponent.camera

        drawBackground(grid, mainCamera)

        if (Kore.input.isKeyDown(Keys.KEY_CONTROL) && Kore.input.isKeyJustDown(Keys.KEY_Z))
            editor.undo()

        if (Kore.input.isKeyDown(Keys.KEY_CONTROL) && Kore.input.isKeyJustDown(Keys.KEY_Y))
            editor.redo()

        if (Kore.input.isKeyDown(Keys.KEY_CONTROL) && Kore.input.isKeyJustDown(Keys.KEY_C))
            copySelection()

        if (Kore.input.isKeyDown(Keys.KEY_CONTROL) && Kore.input.isKeyJustDown(Keys.KEY_V))
            pasteSelection()

        if (Kore.input.isKeyJustDown(Keys.KEY_DELETE))
            deleteSelection()

        Game.renderer.render(mainCamera) { it !in mainCameraComponent.excludedLayers }
        Game.renderer.clear()

        if (GameControls.openMenuFromLevel.isTriggered)
            isMenuOpen = !isMenuOpen

        drawToolSelection()
        drawCurrentTool(grid, mainCamera)
        drawCoordinateInfoLine(grid, mainCamera)

        if (isMenuOpen) {
            ui.begin()
            ui.group(Color(0xFFF5CCFF.toInt())) {
                ui.textButton("Resume") {
                    isMenuOpen = false
                }
                ui.textButton("To Menu") {
                    println("To menu") //TODO
                }
                ui.textButton("Settings") {
                    println("Settings") //TODO
                }
                ui.textButton("Close Game") {
                    Kore.stop()
                }
            }
            ui.end()
        }

        return this
    }

    override fun onDestroy() {
        scene.dispose()
        ui.dispose()
    }
}