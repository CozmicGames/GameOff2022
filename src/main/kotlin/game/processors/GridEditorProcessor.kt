package game.processors

import com.cozmicgames.Kore
import com.cozmicgames.input
import com.cozmicgames.input.MouseButton
import com.cozmicgames.input.MouseButtons
import com.cozmicgames.log
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.OrthographicCamera
import com.cozmicgames.utils.maths.unproject
import engine.Game
import engine.graphics.asRegion
import engine.graphics.drawRect
import engine.scene.GameObject
import engine.scene.SceneProcessor
import engine.scene.components.TransformComponent
import engine.scene.findGameObjectInChildren
import engine.scene.findGameObjectsWithComponent
import game.components.CameraComponent
import game.components.GridCellComponent
import game.components.GridComponent
import kotlin.math.ceil
import kotlin.math.floor

class GridEditorProcessor(val gridObject: GameObject) : SceneProcessor() {
    companion object {
        private val BACKGROUND_COLOR = Color.GRAY
    }

    override fun shouldProcess(delta: Float) = true

    override fun process(delta: Float) {
        val scene = scene ?: return

        if (gridObject !in scene.gameObjects) {
            Kore.log.error(this::class, "Grid gameobject is removed from scene while editing.")
            return
        }

        val grid = gridObject.getComponent<GridComponent>()

        if (grid == null) {
            Kore.log.error(this::class, "Grid component is removed from grid gameobject while editing.")
            return
        }

        var camera: OrthographicCamera? = null

        for (gameObject in scene.activeGameObjects) {
            val cameraComponent = gameObject.getComponent<CameraComponent>() ?: continue

            if (cameraComponent.isMainCamera) {
                camera = cameraComponent.camera
                break
            }
        }

        if (camera == null) {
            Kore.log.error(this::class, "No main camera is active in scene while editing.")
            return
        }

        val backgroundTexture = Game.textures["assets/images/grid_background_8x8.png"] ?: Game.graphics2d.missingTexture.asRegion()
        val hoverTexture = Game.textures["assets/images/grid_hover.png"] ?: Game.graphics2d.missingTexture.asRegion()

        val backgroundTileWidth = 8 * grid.cellSize
        val backgroundTileHeight = 8 * grid.cellSize

        val numBackgroundTilesX = ceil(camera.rectangle.width / backgroundTileWidth).toInt() + 1
        val numBackgroundTilesY = ceil(camera.rectangle.height / backgroundTileHeight).toInt() + 1

        var backgroundTileX = floor((camera.position.x - camera.rectangle.width * 0.5f) / backgroundTileWidth) * backgroundTileWidth

        repeat(numBackgroundTilesX) { x ->
            var backgroundTileY = floor((camera.position.y - camera.rectangle.height * 0.5f) / backgroundTileHeight) * backgroundTileHeight

            repeat(numBackgroundTilesY) { y ->
                Game.renderer.submit(grid.layer - 1, backgroundTexture.texture, "default", false, false) {
                    it.drawRect(backgroundTileX, backgroundTileY, backgroundTileWidth, backgroundTileHeight, color = BACKGROUND_COLOR, u0 = backgroundTexture.u0, v0 = backgroundTexture.v0, u1 = backgroundTexture.u1, v1 = backgroundTexture.v1)
                }

                backgroundTileY += backgroundTileHeight
            }

            backgroundTileX += backgroundTileWidth
        }

        val (worldX, worldY, _) = camera.unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())

        val tileX = floor(worldX / grid.cellSize).toInt()
        val tileY = floor(worldY / grid.cellSize).toInt()

        Game.renderer.submit(grid.layer - 1, hoverTexture.texture, "default", false, false) {
            it.drawRect(tileX * grid.cellSize, tileY * grid.cellSize, grid.cellSize, grid.cellSize, u0 = hoverTexture.u0, v0 = hoverTexture.v0, u1 = hoverTexture.u1, v1 = hoverTexture.v1)
        }

        if (Kore.input.isButtonJustDown(MouseButtons.LEFT))
            addTile(tileX, tileY)

        if (Kore.input.isButtonJustDown(MouseButtons.RIGHT))
            removeTile(tileX, tileY)
    }

    private fun addTile(tileX: Int, tileY: Int) {
        val scene = scene ?: return

        val tileObject = scene.addGameObject {
            addComponent<GridCellComponent> {
                cellX = tileX
                cellY = tileY
                tileType = "test" //TODO: Implement
            }
        }

        tileObject.parent = gridObject
    }

    private fun removeTile(tileX: Int, tileY: Int) {
        val scene = scene ?: return

        val cellObject = gridObject.findGameObjectInChildren {
            it.getComponent<GridCellComponent>()?.let { it.cellX == tileX && it.cellY == tileY } == true
        } ?: return

        scene.removeGameObject(cellObject)
    }
}