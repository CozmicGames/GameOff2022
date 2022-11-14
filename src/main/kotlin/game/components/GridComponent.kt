package game.components

import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.Updateable
import engine.Game
import engine.graphics.Drawable
import engine.graphics.buildDrawable
import engine.scene.Component
import engine.scene.GameObject
import engine.scene.components.DrawableProviderComponent
import engine.scene.components.TransformComponent
import game.level.TileSet

class GridComponent : Component(), Updateable {
    var cellSize = 32.0f
    var tileSet = "<missing>"
    var isCollidable = false
    var layer = 0

    internal var isDirty = true

    private lateinit var drawableProviderComponent: DrawableProviderComponent
    private lateinit var transformComponent: TransformComponent

    override fun onAdded() {
        drawableProviderComponent = gameObject.getOrAddComponent()
        transformComponent = gameObject.getOrAddComponent()
        transformComponent.transform.addChangeListener {
            isDirty = true
        }
    }

    override fun update(delta: Float) {
        if (!isDirty)
            return

        val tileSet = Game.tileSets[tileSet]

        val cells = hashMapOf<TileSet.TileType, MutableList<GridCellComponent>>()

        fun findCellComponents(gameObject: GameObject) {
            for (child in gameObject.children) {
                val gridCellComponent = child.getComponent<GridCellComponent>() ?: continue
                cells.getOrPut(tileSet[gridCellComponent.tileType]) { arrayListOf() } += gridCellComponent
            }
        }

        findCellComponents(gameObject)

        drawableProviderComponent.drawables.clear()

        cells.forEach { (tileType, list) ->
            val drawable = buildDrawable(tileType.material, layer) {
                var currentIndex = 0

                list.forEach {
                    val posX = it.cellX * cellSize + transformComponent.transform.x
                    val posY = it.cellY * cellSize + transformComponent.transform.y

                    vertex {
                        x = posX
                        y = posY
                        u = 0.0f
                        v = 0.0f
                    }

                    vertex {
                        x = posX + cellSize
                        y = posY
                        u = 1.0f
                        v = 0.0f
                    }

                    vertex {
                        x = posX + cellSize
                        y = posY + cellSize
                        u = 1.0f
                        v = 1.0f
                    }

                    vertex {
                        x = posX
                        y = posY + cellSize
                        u = 0.0f
                        v = 1.0f
                    }

                    index(currentIndex)
                    index(currentIndex + 1)
                    index(currentIndex + 2)
                    index(currentIndex)
                    index(currentIndex + 2)
                    index(currentIndex + 3)

                    currentIndex += 4
                }
            }

            if (drawable.verticesCount > 0 && drawable.indicesCount > 0)
                drawableProviderComponent.drawables += drawable
        }

        isDirty = false
    }

    override fun read(properties: Properties) {
        properties.getFloat("cellSize")?.let { cellSize = it }
        properties.getString("tileSet")?.let { tileSet = it }
        properties.getBoolean("isCollidable")?.let { isCollidable = it }
        properties.getInt("layer")?.let { layer = it }
    }

    override fun write(properties: Properties) {
        properties.setFloat("cellSize", cellSize)
        properties.setString("tileSet", tileSet)
        properties.setBoolean("isCollidable", isCollidable)
        properties.setInt("layer", layer)
    }
}