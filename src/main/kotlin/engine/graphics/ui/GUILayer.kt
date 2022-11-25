package engine.graphics.ui

import com.cozmicgames.graphics.gpu.ScissorRect
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.intersectRectRect
import engine.graphics.Renderer
import java.awt.datatransfer.FlavorListener
import kotlin.math.max
import kotlin.math.min

class GUILayer {
    private val visibility = GUIVisibility()

    val commands = GUICommandList()

    fun addElement(minX: Float, minY: Float, maxX: Float, maxY: Float) {
        if (maxX - minX <= 0.0f || maxY - minY <= 0.0f)
            return

        visibility.add(minX, minY, maxX - minX, maxY - minY)
    }

    fun contains(x: Float, y: Float): Boolean {
        return visibility.contains(x, y)
    }

    fun addToVisibility(visibility: GUIVisibility) {
        this.visibility.nodes.forEach {
            visibility.add(it.x, it.y, it.width, it.height)
        }
    }

    fun process(renderer: Renderer) {
        commands.process(renderer)
        visibility.reset()
    }
}