package game.extensions

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import engine.Game
import engine.graphics.shaders.DefaultShader
import engine.graphics.ui.*
import engine.graphics.ui.widgets.imageButton
import engine.materials.Material
import game.level.LevelEditor
import game.level.editorStyle
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

fun GUI.editImageButton(size: Float, action: () -> Unit): GUIElement {
    val element = getLastElement()

    Game.gui.offset(element.width - size * 1.25f, size * 0.25f) {
        Game.gui.imageButton(Game.textures["assets/images/edit_tiletype.png"], size, action = action)
    }

    return element
}

fun GUI.multilineList(maxWidth: Float, spacing: Float, backgroundColor: Color? = null, nextElement: () -> (() -> GUIElement)?) = group(backgroundColor) {
    var elementForNextLine: (() -> GUIElement)?

    while (true) {
        elementForNextLine = null

        sameLine {
            var width = 0.0f

            spacing(spacing * 0.5f)

            elementForNextLine?.let {
                val elementWidth = it().width
                spacing(spacing)
                width += spacing + elementWidth
            }

            while (true) {
                val element = nextElement()

                if (element == null) {
                    spacing(spacing * 0.5f)
                    break
                }

                val elementWidth = getElementSize(element).width

                if (width + spacing + elementWidth + spacing * 0.5f <= maxWidth) {
                    element()
                    spacing(spacing)
                    width += spacing + elementWidth
                } else {
                    spacing(spacing * 0.5f)
                    elementForNextLine = element
                    break
                }
            }
        }

        if (elementForNextLine == null)
            break
    }
}

fun GUI.multilineListWithSameElementWidths(maxWidth: Float, elementWidth: Float, minSpacing: Float? = null, backgroundColor: Color? = null, nextElement: () -> (() -> GUIElement)?) = group(backgroundColor) {
    val elementsPerLine = maxWidth.toInt() / (elementWidth + (minSpacing ?: 0.0f)).toInt() - 1
    val elementSpacing = (maxWidth - (elementsPerLine + 1) * elementWidth) / elementsPerLine

    var hasMoreElements = true

    while (hasMoreElements) {
        sameLine {
            spacing(elementSpacing * 0.5f)

            var count = 0
            while (count++ < elementsPerLine) {
                val element = nextElement()

                if (element == null) {
                    spacing(elementSpacing * 0.5f)
                    hasMoreElements = false
                    break
                }

                spacing(elementSpacing)
                element()

                if (count == elementsPerLine - 1)
                    spacing(elementSpacing * 0.5f)
            }
        }
    }
}
