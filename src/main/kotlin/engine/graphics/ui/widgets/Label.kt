package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Rectangle
import engine.graphics.font.GlyphLayout
import engine.graphics.font.HAlign
import engine.graphics.ui.*
import kotlin.math.max
import kotlin.math.min

/**
 * Adds a label element.
 *
 * @param text The text of the label.
 * @param backgroundColor The background color of the label. Defaults to [style.backgroundColor]. If null, no background is drawn.
 * @param maxWidth An optional maximum width of this label.
 * @param minWidth An optional minumum width of this label.
 * @param align How to align the text inside this label if [minWidth] exceeds the size needed by the text.
 */
fun GUI.label(text: String, backgroundColor: Color? = skin.backgroundColor, maxWidth: Float? = null, minWidth: Float? = null, align: HAlign = HAlign.LEFT): GUIElement {
    val (x, y) = getLastElement()
    val layout = GlyphLayout(text, drawableFont)
    val textX = x + skin.elementPadding
    val textY = y + skin.elementPadding
    var textWidth = layout.width + 2.0f * skin.elementPadding
    val textHeight = layout.height + 2.0f * skin.elementPadding

    if (maxWidth != null) {
        val clipRectangle = Rectangle(x, y, maxWidth, textHeight)

        if (backgroundColor != null)
            currentCommandList.drawRectFilled(x, y, textWidth, textHeight, skin.roundedCorners, skin.cornerRounding, backgroundColor)

        currentCommandList.drawText(textX, textY, layout, skin.fontColor, clipRectangle)

        textWidth = min(textWidth, maxWidth)
    } else if (minWidth != null) {
        val labelWidth = max(textWidth, minWidth)

        if (backgroundColor != null)
            currentCommandList.drawRectFilled(x, y, labelWidth, textHeight, skin.roundedCorners, skin.cornerRounding, backgroundColor)

        val alignedTextX = when (align) {
            HAlign.LEFT -> textX
            HAlign.CENTER -> textX + (labelWidth - textWidth) * 0.5f
            HAlign.RIGHT -> textX + labelWidth - textWidth
        }

        currentCommandList.drawText(alignedTextX, textY, layout, skin.fontColor)

        textWidth = labelWidth
    } else {
        if (backgroundColor != null)
            currentCommandList.drawRectFilled(x, y, textWidth, textHeight, skin.roundedCorners, skin.cornerRounding, backgroundColor)

        currentCommandList.drawText(textX, textY, layout, skin.fontColor)
    }

    return setLastElement(x, y, textWidth, textHeight)
}
