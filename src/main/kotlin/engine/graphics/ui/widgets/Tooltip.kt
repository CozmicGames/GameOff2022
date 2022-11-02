package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.font.GlyphLayout
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled
import engine.graphics.ui.drawText

fun GUI.tooltip(element: GUIElement, text: String, backgroundColor: Color? = skin.backgroundColor) {
    if (!shouldShowTooltip)
        return

    val layout = GlyphLayout(text, drawableFont)

    val x = touchPosition.x
    val y = touchPosition.y - (layout.height + skin.elementPadding * 2.0f)

    val textX = x + skin.elementPadding
    val textY = y + skin.elementPadding
    val width = layout.width + 2.0f * skin.elementPadding
    val height = layout.height + 2.0f * skin.elementPadding

    val rectangle = Rectangle()
    rectangle.x = element.x
    rectangle.y = element.y
    rectangle.width = element.width
    rectangle.height = element.height

    if (GUI.State.HOVERED in getState(rectangle)) {
        layerUp {
            if (backgroundColor != null)
                currentCommandList.drawRectFilled(x, y, width, height, skin.roundedCorners, skin.cornerRounding, backgroundColor)

            currentCommandList.drawText(textX, textY, layout, skin.fontColor)
        }
    }
}
