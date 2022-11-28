package game.level

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input
import com.cozmicgames.input.Keys
import com.cozmicgames.input.MouseButtons
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.collections.Array2D
import com.cozmicgames.utils.maths.OrthographicCamera
import com.cozmicgames.utils.maths.Vector2
import com.cozmicgames.utils.maths.unproject
import engine.Game
import engine.graphics.asRegion
import engine.graphics.drawPathStroke
import engine.graphics.drawRect
import engine.graphics.ui.GUIVisibility
import engine.graphics.ui.widgets.*
import engine.scene.Scene
import engine.scene.findGameObjectByComponent
import engine.scene.findGameObjectsWithComponent
import engine.utils.FreeCameraControllerComponent
import game.components.CameraComponent
import game.components.GridComponent
import game.components.getCellType
import game.extensions.editable
import game.extensions.plusButton
import kotlin.math.ceil
import kotlin.math.floor

class LevelEditor(val scene: Scene) {
    sealed interface ReturnState {
        object None : ReturnState

        object Menu : ReturnState

        class EditTileType(val tileType: String, val tileSet: String) : ReturnState
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

    private val commandExecutor = EditorCommandExecutor()
    private val scrollAmount = Vector2()

    private var copiedRegion: Array2D<String?>? = null
    private var selectionRegion: GridRegion? = null
    private var currentTool = ToolType.PENCIL
    private var currentType = "test"
    private var currentGridLayer = findGridLayerUp(-Int.MAX_VALUE)

    init {
        addCameraControls()
    }

    private fun findGridLayerUp(layer: Int): Int? {
        val gridComponents = arrayListOf<GridComponent>()

        scene.findGameObjectsWithComponent<GridComponent> {
            it.getComponent<GridComponent>()?.let {
                gridComponents += it
            }
        }

        var closestGridComponent: GridComponent? = null

        for (gridComponent in gridComponents) {
            if (gridComponent.layer <= layer)
                continue

            if (closestGridComponent == null)
                closestGridComponent = gridComponent
            else if (closestGridComponent.layer > gridComponent.layer)
                closestGridComponent = gridComponent
        }

        return closestGridComponent?.layer
    }

    private fun findGridLayerDown(layer: Int): Int? {
        val gridComponents = arrayListOf<GridComponent>()

        scene.findGameObjectsWithComponent<GridComponent> {
            it.getComponent<GridComponent>()?.let {
                gridComponents += it
            }
        }

        var closestGridComponent: GridComponent? = null

        for (gridComponent in gridComponents) {
            if (gridComponent.layer >= layer)
                continue

            if (closestGridComponent == null)
                closestGridComponent = gridComponent
            else if (closestGridComponent.layer < gridComponent.layer)
                closestGridComponent = gridComponent
        }

        return closestGridComponent?.layer
    }

    fun addCameraControls() {
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

    fun removeCameraControls() {
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
                commandExecutor.setTileTypes(it, source)
            }
        }
    }

    private fun deleteSelection() {
        selectionRegion?.let {
            it.setTiles { _, _ -> null }
        }
    }

    private fun drawBackground(grid: GridComponent, camera: OrthographicCamera) {
        val backgroundTexture = Game.textures["assets/images/grid_background_8x8.png"]

        val backgroundTileWidth = 8 * grid.cellSize
        val backgroundTileHeight = 8 * grid.cellSize

        val numBackgroundTilesX = ceil(camera.rectangle.width / backgroundTileWidth).toInt() + 1
        val numBackgroundTilesY = ceil(camera.rectangle.height / backgroundTileHeight).toInt() + 1

        var backgroundTileX = floor((camera.position.x - camera.rectangle.width * 0.5f) / backgroundTileWidth) * backgroundTileWidth

        repeat(numBackgroundTilesX) {
            var backgroundTileY = floor((camera.position.y - camera.rectangle.height * 0.5f) / backgroundTileHeight) * backgroundTileHeight

            repeat(numBackgroundTilesY) {
                Game.renderer.submit(grid.layer - 1, backgroundTexture.texture, "default", false, false) {
                    it.drawRect(backgroundTileX, backgroundTileY, backgroundTileWidth, backgroundTileHeight, color = Color.LIGHT_GRAY, u0 = backgroundTexture.u0, v0 = backgroundTexture.v0, u1 = backgroundTexture.u1, v1 = backgroundTexture.v1)
                }

                backgroundTileY += backgroundTileHeight
            }

            backgroundTileX += backgroundTileWidth
        }
    }

    fun drawCurrentTool(grid: GridComponent, camera: OrthographicCamera, visibility: GUIVisibility) {
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

        if (Vector2(Kore.input.x.toFloat(), Kore.graphics.height - Kore.input.y.toFloat()) in visibility)
            return

        when (currentTool) {
            ToolType.PENCIL -> {
                if (Game.gui.isInteractionEnabled) {
                    val potentialMaterialName = Game.tileSets[grid.tileSet][currentType].getMaterial(grid, tileX, tileY)
                    val currentTypeTexture = Game.materials[potentialMaterialName]?.colorTexturePath?.let {
                        Game.textures[it]
                    } ?: Game.graphics2d.missingTexture.asRegion()

                    Game.renderer.submit(grid.layer - 1, currentTypeTexture.texture, "default", false, false) {
                        it.drawRect(tileX * grid.cellSize, tileY * grid.cellSize, grid.cellSize, grid.cellSize, Color(1.0f, 1.0f, 1.0f, 0.5f), u0 = currentTypeTexture.u0, v0 = currentTypeTexture.v0, u1 = currentTypeTexture.u1, v1 = currentTypeTexture.v1)
                    }

                    if (Kore.input.isButtonDown(MouseButtons.LEFT) && grid.getCellType(tileX, tileY) != currentType)
                        commandExecutor.setTileType(grid, tileX, tileY, currentType)

                    if (Kore.input.isButtonDown(MouseButtons.RIGHT) && grid.getCellType(tileX, tileY) != null)
                        commandExecutor.setTileType(grid, tileX, tileY, null)
                }
            }
            ToolType.DELETE -> {
                if (Game.gui.isInteractionEnabled) {
                    if ((Kore.input.isButtonDown(MouseButtons.LEFT) || Kore.input.isButtonDown(MouseButtons.RIGHT)) && grid.getCellType(tileX, tileY) != null)
                        commandExecutor.setTileType(grid, tileX, tileY, null)
                }
            }
            ToolType.SELECT -> {
                if (Game.gui.isInteractionEnabled) {
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
            }
            ToolType.PICK -> {
                if (Game.gui.isInteractionEnabled) {
                    if (Kore.input.isButtonJustDown(MouseButtons.LEFT)) {
                        val type = grid.getCellType(tileX, tileY)
                        if (type != null)
                            currentType = type
                    }
                }
            }
            else -> {}
        }
    }

    fun drawToolSelection(setReturnState: (ReturnState) -> Unit) {
        Game.gui.setLastElement(Game.gui.absolute(Kore.graphics.width - Game.editorStyle.toolImageSize, 0.0f))
        Game.gui.group(Game.editorStyle.panelContentBackgroundColor) {
            ToolType.values().forEach {
                val texture = Game.textures[it.texture]
                when (it) {
                    ToolType.FILL -> Game.gui.imageButton(texture, Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize) {
                        selectionRegion?.let {
                            commandExecutor.setTileTypes(it, currentType)
                        }
                    }
                    ToolType.UNDO -> Game.gui.imageButton(texture, Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize) {
                        commandExecutor.undo()
                    }
                    ToolType.REDO -> Game.gui.imageButton(texture, Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize) {
                        commandExecutor.redo()
                    }
                    ToolType.COPY -> Game.gui.imageButton(texture, Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize) {
                        copySelection()
                    }
                    ToolType.PASTE -> Game.gui.imageButton(texture, Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize) {
                        pasteSelection()
                    }
                    ToolType.SETTINGS -> Game.gui.imageButton(texture, Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize) {
                        setReturnState(ReturnState.Menu)
                    }
                    else -> Game.gui.selectableImage(texture, Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize, currentTool == it) {
                        selectionRegion = null
                        currentTool = it
                    }
                }
            }
        }
    }

    fun drawCoordinateInfoLine(grid: GridComponent, camera: OrthographicCamera) {
        Game.gui.setLastElement(Game.gui.absolute(0.0f, Kore.graphics.height - Game.gui.skin.elementSize))
        Game.gui.group(Game.editorStyle.panelContentBackgroundColor) {
            Game.gui.sameLine {
                val (worldX, worldY, _) = camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())

                val tileX = floor(worldX / grid.cellSize).toInt()
                val tileY = floor(worldY / grid.cellSize).toInt()

                Game.gui.label("Cursor: $tileX, $tileY", null)

                selectionRegion?.let {
                    Game.gui.spacing()
                    Game.gui.label("Selected: ${it.width} x ${it.height}", null)
                }

                copiedRegion?.let {
                    Game.gui.spacing()
                    Game.gui.label("Copied: ${it.width} x ${it.height}", null)
                }
            }
        }
    }

    fun drawTypeSelection(grid: GridComponent, setReturnState: (ReturnState) -> Unit) {
        Game.gui.setLastElement(Game.gui.absolute(0.0f, 0.0f))
        //Game.gui.panel(Game.editorStyle.toolImageSize * 5f, 50.0f, scrollAmount, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
        //    Game.gui.label("Tile Types")
        //}) {
        //} //TODO:

        Game.gui.group(Game.editorStyle.panelContentBackgroundColor) {
            Game.gui.scrollArea(Game.editorStyle.toolImageSize * 5f, scroll = scrollAmount) {
                val tileSet = Game.tileSets[grid.tileSet]

                for (name in tileSet.ids) {
                    if (name !in tileSet)
                        continue

                    val tileType = tileSet[name]
                    val isSelected = currentType == name

                    Game.gui.editable({
                        val texture = Game.textures[Game.materials[tileType.defaultMaterial]?.colorTexturePath ?: "<missing>"]

                        Game.gui.selectableImage(texture, Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize, isSelected) {
                            currentType = name
                        }
                    }, Game.editorStyle.toolImageSize * 1.0f / 3.0f) {
                        setReturnState(ReturnState.EditTileType(name, grid.tileSet))
                    }
                }

                Game.gui.plusButton(Game.editorStyle.toolImageSize, Game.editorStyle.toolImageSize) {
                    tileSet.addType()
                }
            }
        }
    }

    fun onFrame(delta: Float): ReturnState {
        if (Game.gui.isInteractionEnabled) {
            if (Kore.input.isKeyDown(Keys.KEY_CONTROL) && Kore.input.isKeyJustDown(Keys.KEY_Z))
                commandExecutor.undo()

            if (Kore.input.isKeyDown(Keys.KEY_CONTROL) && Kore.input.isKeyJustDown(Keys.KEY_Y))
                commandExecutor.redo()

            if (Kore.input.isKeyDown(Keys.KEY_CONTROL) && Kore.input.isKeyJustDown(Keys.KEY_C))
                copySelection()

            if (Kore.input.isKeyDown(Keys.KEY_CONTROL) && Kore.input.isKeyJustDown(Keys.KEY_V))
                pasteSelection()

            if (Kore.input.isKeyJustDown(Keys.KEY_DELETE))
                deleteSelection()
        }

        scene.update(delta)

        if (currentGridLayer == null)
            currentGridLayer = findGridLayerUp(-Int.MAX_VALUE)

        val gridObject = scene.findGameObjectByComponent<GridComponent> { it.getComponent<GridComponent>()?.layer == currentGridLayer }

        var mainCameraComponent: CameraComponent? = null

        for (gameObject in scene.activeGameObjects) {
            val cameraComponent = gameObject.getComponent<CameraComponent>() ?: continue

            if (cameraComponent.isMainCamera) {
                mainCameraComponent = cameraComponent
                break
            }
        }

        mainCameraComponent ?: return ReturnState.None

        mainCameraComponent.gameObject.getComponent<FreeCameraControllerComponent>()?.isEnabled = Game.gui.isInteractionEnabled

        val grid = gridObject?.getComponent<GridComponent>() ?: return ReturnState.None
        val mainCamera = mainCameraComponent.camera

        var returnState: ReturnState = ReturnState.None

        drawBackground(grid, mainCamera)

        Game.renderer.render(mainCamera) { it !in mainCameraComponent.excludedLayers }
        Game.renderer.clear()

        Game.gui.begin()
        drawToolSelection { returnState = it }
        drawCoordinateInfoLine(grid, mainCamera)
        drawTypeSelection(grid) { returnState = it }
        val visibility = Game.gui.getCompleteVisibility()
        Game.gui.end()

        drawCurrentTool(grid, mainCamera, visibility)

        return returnState
    }
}