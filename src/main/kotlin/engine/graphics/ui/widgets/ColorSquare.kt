package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled

/**
 * Adds a color square element.
 *
 * @param color The color.
 * @param size The size of the square. Defaults to [style.elementSize].
 */
fun GUI.colorSquare(color: Color, size: Float = skin.elementSize): GUIElement {
    val (x, y) = getLastElement()
    currentCommandList.drawRectFilled(x, y, size, size, skin.roundedCorners, skin.cornerRounding, color)
    return setLastElement(x, y, size, size)
}
