package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.TextureRegion
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.*

/**
 * Adds a text button element.
 *
 * @param text The text of the button.
 * @param texture An optional texture to display behind the text.
 * @param overrideFontColor A color that will be used for the text if not null.
 * @param action The function to execute when the button is clicked.
 */
fun GUI.textButton(text: String, texture: TextureRegion? = null, overrideFontColor: Color? = null, action: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y

    val layout = GlyphLayout(text, drawableFont)
    val textX = x + skin.elementPadding
    val textY = y + skin.elementPadding

    rectangle.width = layout.width + skin.elementPadding * 2.0f
    rectangle.height = layout.height + skin.elementPadding * 2.0f

    if (texture != null)
        rectangle.width += skin.contentSize + skin.elementPadding * 2.0f

    val state = getState(rectangle, GUI.TouchBehaviour.ONCE_UP)

    val color = if (GUI.State.ACTIVE in state) {
        action()
        skin.highlightColor
    } else if (GUI.State.HOVERED in state)
        skin.hoverColor
    else
        skin.normalColor

    currentCommandList.drawRectFilled(rectangle.x, rectangle.y, rectangle.width, rectangle.height, skin.roundedCorners, skin.cornerRounding, color)
    currentCommandList.drawText(textX, textY, layout, overrideFontColor ?: skin.fontColor)

    if (texture != null)
        currentCommandList.drawImage(textX + skin.elementPadding, textY, skin.contentSize, skin.contentSize, texture, overrideFontColor ?: skin.fontColor)

    return setLastElement(x, y, rectangle.width, rectangle.height)
}
