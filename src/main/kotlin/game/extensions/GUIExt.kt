package game.extensions

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import engine.Game
import engine.graphics.shaders.DefaultShader
import engine.graphics.ui.*
import engine.materials.Material
import java.lang.Float.min
import kotlin.math.sqrt

fun GUI.plusButton(width: Float = skin.elementSize, height: Float = width, action: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

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

fun GUI.minusButton(width: Float = skin.elementSize, height: Float = width, action: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

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

fun GUI.upButton(width: Float = skin.elementSize, height: Float = width, action: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.TouchBehaviour.ONCE_UP)
    val triangleSize = min(width, height) * 0.75f

    val color = if (GUI.State.ACTIVE in state) {
        action()
        skin.highlightColor
    } else if (GUI.State.HOVERED in state)
        skin.hoverColor
    else
        skin.normalColor

    currentCommandList.drawRectFilled(rectangle.x, rectangle.y, rectangle.width, rectangle.height, skin.roundedCorners, skin.cornerRounding, color)

    val x0 = rectangle.centerX
    val y0 = rectangle.centerY - (sqrt(3.0f) / 3.0f) * triangleSize

    val x1 = rectangle.centerX - triangleSize * 0.5f
    val y1 = rectangle.centerY + (sqrt(3.0f) / 4.0f) * triangleSize

    val x2 = rectangle.centerX + triangleSize * 0.5f
    val y2 = rectangle.centerY + (sqrt(3.0f) / 4.0f) * triangleSize

    currentCommandList.drawTriangleFilled(x0, y0, x1, y1, x2, y2, skin.fontColor)

    return setLastElement(x, y, width, height)
}

fun GUI.downButton(width: Float = skin.elementSize, height: Float = width, action: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = width
    rectangle.height = height

    val state = getState(rectangle, GUI.TouchBehaviour.ONCE_UP)
    val triangleSize = min(width, height) * 0.75f

    val color = if (GUI.State.ACTIVE in state) {
        action()
        skin.highlightColor
    } else if (GUI.State.HOVERED in state)
        skin.hoverColor
    else
        skin.normalColor

    currentCommandList.drawRectFilled(rectangle.x, rectangle.y, rectangle.width, rectangle.height, skin.roundedCorners, skin.cornerRounding, color)

    val x0 = rectangle.centerX
    val y0 = rectangle.centerY + (sqrt(3.0f) / 4.0f) * triangleSize

    val x1 = rectangle.centerX - triangleSize * 0.5f
    val y1 = rectangle.centerY - (sqrt(3.0f) / 4.0f) * triangleSize

    val x2 = rectangle.centerX + triangleSize * 0.5f
    val y2 = rectangle.centerY - (sqrt(3.0f) / 4.0f) * triangleSize

    currentCommandList.drawTriangleFilled(x0, y0, x1, y1, x2, y2, skin.fontColor)

    return setLastElement(x, y, width, height)
}

fun GUI.materialPreview(material: Material, width: Float = skin.elementSize, height: Float = width, borderThickness: Float = skin.strokeThickness): GUIElement {
    val (x, y) = getLastElement()

    currentCommandList.addCommand {
        withShader(Game.shaders[material.shader] ?: DefaultShader) {
            draw(Game.textures[material.colorTexturePath], x, y, width, height, material.color)
        }
    }

    if (borderThickness > 0.0f)
        currentCommandList.drawRect(x, y, width, height, skin.roundedCorners, skin.cornerRounding, borderThickness, skin.normalColor)

    return setLastElement(x, y, width, height)
}
