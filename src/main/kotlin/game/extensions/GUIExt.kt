package game.extensions

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRect
import engine.graphics.ui.drawRectFilled
import java.lang.Float.min

fun GUI.plusButton(width: Float = skin.elementSize, height: Float = skin.elementSize, element: GUIElement = getLastElement(), action: () -> Unit): GUIElement {
    val (x, y) = element

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.TouchBehaviour.ONCE_UP)
    val plusThickness = min(width, height) * 0.2f
    val borderSize = min(width, height) / skin.elementSize

    val color = if (GUI.State.ACTIVE in state) {
        action()
        skin.highlightColor
    } else if (GUI.State.HOVERED in state)
        skin.hoverColor
    else
        skin.normalColor

    currentCommandList.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, skin.roundedCorners, skin.cornerRounding, borderSize, color)
    currentCommandList.drawRectFilled(rectangle.centerX - plusThickness * 0.5f, rectangle.y + plusThickness, plusThickness, rectangle.height - plusThickness * 2.0f, skin.roundedCorners, skin.cornerRounding, color)
    currentCommandList.drawRectFilled(rectangle.x + plusThickness, rectangle.centerY - plusThickness * 0.5f, rectangle.width - plusThickness * 2.0f, plusThickness, skin.roundedCorners, skin.cornerRounding, color)

    return setLastElement(x, y, width, height)
}

fun GUI.minusButton(width: Float = skin.elementSize, height: Float = skin.elementSize, element: GUIElement = getLastElement(), action: () -> Unit): GUIElement {
    val (x, y) = element

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.TouchBehaviour.ONCE_UP)
    val plusThickness = min(width, height) * 0.2f
    val borderSize = min(width, height) / skin.elementSize

    val color = if (GUI.State.ACTIVE in state) {
        action()
        skin.highlightColor
    } else if (GUI.State.HOVERED in state)
        skin.hoverColor
    else
        skin.normalColor

    currentCommandList.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, skin.roundedCorners, skin.cornerRounding, borderSize, color)
    currentCommandList.drawRectFilled(rectangle.x + plusThickness, rectangle.centerY - plusThickness * 0.5f, rectangle.width - plusThickness * 2.0f, plusThickness, skin.roundedCorners, skin.cornerRounding, color)

    return setLastElement(x, y, width, height)
}
