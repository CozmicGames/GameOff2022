package game.level

import com.cozmicgames.utils.Properties
import engine.scene.Component

class GridCellComponent : Component() {
    var tileX = 0
    var tileY = 0

    override fun read(properties: Properties) {
        properties.getInt("tileX")?.let { tileX = it }
        properties.getInt("tileY")?.let { tileY = it }
    }

    override fun write(properties: Properties) {
        properties.setInt("tileX", tileX)
        properties.setInt("tileY", tileY)
    }
}