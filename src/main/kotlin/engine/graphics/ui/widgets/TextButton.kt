package engine.graphics.ui.widgets

import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled
import engine.graphics.ui.drawText

/**
 * Adds a text button element.
 *
 * @param text The text of the button.
 * @param action The function to execute when the button is clicked.
 */
fun GUI.textButton(text: String, action: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y

    val layout = GlyphLayout(text, drawableFont)
    val textX = x + skin.elementPadding
    val textY = y + skin.elementPadding

    rectangle.width = layout.width + skin.elementPadding * 2.0f
    rectangle.height = layout.height + skin.elementPadding * 2.0f

    val state = getState(rectangle, GUI.ButtonBehaviour.DEFAULT)

    val color = if (GUI.State.ACTIVE in state) {
        action()
        skin.highlightColor
    } else if (GUI.State.HOVERED in state)
        skin.hoverColor
    else
        skin.normalColor

    currentCommandList.drawRectFilled(rectangle.x, rectangle.y, rectangle.width, rectangle.height, skin.roundedCorners, skin.cornerRounding, color)
    currentCommandList.drawText(textX, textY, layout, skin.fontColor)

    return setLastElement(x, y, rectangle.width, rectangle.height)
}
