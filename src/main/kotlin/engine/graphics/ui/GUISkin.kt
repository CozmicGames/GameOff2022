package engine.graphics.ui

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Corners
import engine.graphics.font.GlyphLayout

/**
 * Represents the style of GUI elements.
 */
open class GUISkin {
    open var font = Kore.graphics.defaultFont
    open var contentSize = 14.0f
    open var elementSize = 20.0f
    open var scrollbarSize = 10.0f
    open var backgroundColor = Color(0x171A23FF)
    open var normalColor = Color(0x2A2F3FFF)
    open var highlightColor = Color(0x4FB742FF)
    open var hoverColor = Color(0x43485BFF)
    open var fontColor = Color.WHITE.copy()
    open var cursorColor = Color(0xFFFFFF99.toInt())
    open var strokeThickness = 1.5f
    open var cornerRounding = 1.5f
    open var roundedCorners = Corners.ALL
    open var scrollSpeed = 5.0f
    open var tooltipDelay = 1.0f

    val elementPadding get() = (elementSize - contentSize) * 0.5f

    open fun drawBorder(x: Float, y: Float, width: Float, height: Float, state: Int) {

    }

    open fun drawBackground(x: Float, y: Float, width: Float, height: Float, state: Int) {

    }

    open fun drawScrollbarHandleHorizontal(x: Float, y: Float, width: Float, height: Float, state: Int) {

    }

    open fun drawScrollbarHandleVertical(x: Float, y: Float, width: Float, height: Float, state: Int) {

    }

    open fun drawProgressBar(x: Float, y: Float, width: Float, height: Float, pogress: Float) {

    }

    open fun drawString(layout: GlyphLayout, state: Int) {

    }

    open fun drawCheckmark(x: Float, y: Float, isChecked: Boolean, state: Int) {

    }

    open fun drawOption(x: Float, y: Float, isChecked: Boolean, state: Int) {

    }
}