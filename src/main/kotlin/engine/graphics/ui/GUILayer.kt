package engine.graphics.ui

import engine.graphics.Renderer

class GUILayer {
    private val visibility = GUIVisibility()

    val commands = GUICommandList()

    fun addElement(element: GUIElement) {
        if (element.width > 0.0f && element.height > 0.0f)
            visibility.add(element.x, element.y, element.width, element.height)
    }

    fun contains(x: Float, y: Float): Boolean {
        return visibility.contains(x, y)
    }

    fun process(renderer: Renderer) {
        commands.process(renderer)
        visibility.reset()
    }
}