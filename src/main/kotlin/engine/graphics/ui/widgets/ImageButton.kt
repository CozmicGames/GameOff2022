package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.TextureRegion
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawImage

/**
 * Adds an image button to the GUI.
 *
 * @param texture The texture to use for the image.
 * @param width The width of the image. Defaults to [style.elementSize].
 * @param height The height of the image. Defaults to the same as [width].
 * @param action The action to perform when the image is clicked.
 */
fun GUI.imageButton(texture: TextureRegion, width: Float = skin.elementSize, height: Float = width, action: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    val color = if (GUI.State.ACTIVE in state) {
        action()
        skin.highlightColor
    } else if (GUI.State.HOVERED in state)
        skin.hoverColor
    else
        Color.WHITE

    currentCommandList.drawImage(x, y, width, height, texture, color)

    return setLastElement(x, y, width, height)
}
