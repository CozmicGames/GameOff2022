package game.level

import com.cozmicgames.Kore
import com.cozmicgames.input
import com.cozmicgames.utils.maths.OrthographicCamera
import com.cozmicgames.utils.maths.unproject
import engine.physics.AxisAlignedRectangleShape
import engine.scene.GameObject
import engine.scene.Scene
import engine.scene.SceneProcessor
import engine.scene.components.*
import engine.scene.findGameObjectsWithComponent
import game.Constants
import game.components.CameraComponent
import kotlin.math.floor

object GridUtils {
    class GridProcessor(val width: Int, val height: Int) : SceneProcessor() {
        override fun shouldProcess(delta: Float) = true

        override fun process(delta: Float) {
            val scene = this.scene ?: return
            var hoverGameObject: GameObject? = null

            scene.findGameObjectsByTag("grid", "hover") { hoverGameObject = it }

            var camera: OrthographicCamera? = null

            scene.findGameObjectsWithComponent<CameraComponent> {
                val cameraComponent = requireNotNull(it.getComponent<CameraComponent>())
                if (cameraComponent.isMainCamera)
                    camera = cameraComponent.camera
            }

            hoverGameObject ?: return
            camera ?: return

            val (worldX, worldY, _) = requireNotNull(camera).unproject(Kore.input.x.toFloat(), Kore.input.y.toFloat())

            val tileX = floor(worldX / Constants.TILE_SIZE + 0.5f).toInt()
            val tileY = floor(worldY / Constants.TILE_SIZE + 0.5f).toInt()

            scene.findGameObjectsByTag("grid") {
                it.isActive = true
            }

            if (tileX < 0 || tileX >= width || tileY < 0 || tileY >= height) {
                hoverGameObject?.isActive = false
                return
            }

            scene.findGameObjectsByTag("grid", "$tileX:$tileY") {
                it.isActive = false
            }

            hoverGameObject?.let {
                it.isActive = true
                val transformComponent = it.getComponent<TransformComponent>()
                transformComponent?.transform?.x = tileX * Constants.TILE_SIZE
                transformComponent?.transform?.y = tileY * Constants.TILE_SIZE
            }
        }
    }

    fun addGrid(scene: Scene, width: Int, height: Int, layer: Int) {
        scene.addSceneProcessor(GridProcessor(width, height))

        repeat(width) { x ->
            repeat(height) { y ->
                scene.addGameObject {
                    addComponent<TransformComponent> {
                        transform.x = x * Constants.TILE_SIZE
                        transform.y = y * Constants.TILE_SIZE
                        transform.scaleX = Constants.TILE_SIZE
                        transform.scaleY = Constants.TILE_SIZE
                    }
                    addComponent<TagComponent> {
                        tags += "grid"
                    }
                    addComponent<ColliderComponent> {
                        isStatic = true
                        gravityScale = 0.0f
                        shape = AxisAlignedRectangleShape()
                    }
                    addComponent<GridCellComponent> {
                        tileX = x
                        tileY = y
                    }
                    addComponent<SpriteComponent> {
                        this.layer = layer
                        material = "assets/materials/tile_grid.material"
                    }
                }
            }
        }

        scene.addGameObject {
            isActive = false
            addComponent<TransformComponent> {
                transform.x = 0.0f
                transform.y = 0.0f
                transform.scaleX = Constants.TILE_SIZE
                transform.scaleY = Constants.TILE_SIZE
            }
            addComponent<TagComponent> {
                tags += "grid"
                tags += "hover"
            }
            addComponent<SpriteComponent> {
                this.layer = layer
                material = "assets/materials/tile_grid_hovered.material"
            }
        }
    }

    fun removeGrid(scene: Scene) {
        scene.findSceneProcessor<GridProcessor>()?.let {
            scene.removeSceneProcessor(it)
        }

        val objectsToRemove = arrayListOf<GameObject>()

        scene.findGameObjectsByTag("grid") {
            objectsToRemove += it
        }

        objectsToRemove.forEach {
            scene.removeGameObject(it)
        }
    }
}