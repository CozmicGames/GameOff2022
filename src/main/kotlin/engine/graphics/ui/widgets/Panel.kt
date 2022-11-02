package engine.graphics.ui.widgets

import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Corners
import com.cozmicgames.utils.maths.Vector2
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.drawRectFilled

/**
 * Adds a panel to the GUI.
 * A panel represents a scrollable area of set size.
 *
 * @param width The width of the panel.
 * @param height The height of the panel.
 * @param scroll The current scroll position of the scroll pane. This function will update the scroll position automatically.
 * @param backgroundColor The panels' background color.
 */
fun GUI.panel(width: Float, height: Float, scroll: Vector2, backgroundColor: Color = skin.backgroundColor, block: () -> Unit): GUIElement {
    lateinit var element: GUIElement

    val commands = recordCommands {
        element = scrollArea(width - (skin.scrollbarSize + skin.elementPadding * 2.0f), height - (skin.scrollbarSize + skin.elementPadding * 2.0f), scroll, block)
    }

    currentCommandList.drawRectFilled(element.x, element.y, width, height, Corners.NONE, 0.0f, backgroundColor)
    currentCommandList.addCommandList(commands)

    return setLastElement(element.x, element.y, width, height)
}

