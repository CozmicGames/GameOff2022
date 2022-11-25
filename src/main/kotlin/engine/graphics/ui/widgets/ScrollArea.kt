package engine.graphics.ui.widgets

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.input
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.Vector2
import engine.graphics.ui.*
import kotlin.math.min

/**
 * Adds a scrollable area to the GUI.
 * A scroll area is a container of GUI elements that can be scrolled.
 *
 * @param maxWidth The maximum width of the scroll pane. If null, the width of the scroll pane is calculated automatically to fit all elements.
 * @param maxHeight The maximum height of the scroll pane. If null, the height of the scroll pane is calculated automatically to fit all elements.
 * @param scroll The current scroll position of the scroll pane. This function will update the scroll position automatically.
 */
fun GUI.scrollArea(maxWidth: Float? = null, maxHeight: Float? = null, scroll: Vector2, block: () -> Unit): GUIElement {
    val (x, y) = getLastElement()

    if (maxWidth == null)
        scroll.x = 0.0f

    if (maxHeight == null)
        scroll.y = 0.0f

    val contentX = x + skin.elementPadding
    val contentY = y + skin.elementPadding

    val scissorRectangle = Rectangle(x, y, maxWidth ?: (Kore.graphics.width - x), maxHeight ?: (Kore.graphics.height - y))
    val previousScissorRectangle = currentScissorRectangle
    val previousUseScissorRectForElementPositioning = useScissorRectForElementPositioning

    currentScissorRectangle = scissorRectangle
    useScissorRectForElementPositioning = false

    lateinit var content: GUIElement

    val commands = recordCommands {
        transient(addToLayer = false) {
            setLastElement(absolute(contentX - scroll.x, contentY - scroll.y))
            content = group { block() }
        }
    }

    currentScissorRectangle = previousScissorRectangle
    useScissorRectForElementPositioning = previousUseScissorRectForElementPositioning

    val contentWidth: Float
    val contentHeight: Float
    val areaWidth: Float
    val areaHeight: Float

    if (maxWidth == null) {
        contentWidth = content.width
        areaWidth = contentWidth + skin.elementPadding * 2.0f
    } else {
        areaWidth = min(content.width + skin.elementPadding * 2.0f, maxWidth) + skin.elementPadding * 2.0f
        contentWidth = areaWidth - skin.elementPadding * 2.0f

        if (scroll.x < 0.0f)
            scroll.x = 0.0f

        if (scroll.x > content.width - contentWidth)
            scroll.x = content.width - contentWidth
    }

    if (maxHeight == null) {
        contentHeight = content.height
        areaHeight = contentHeight + skin.elementPadding * 2.0f
    } else {
        areaHeight = min(content.height + skin.elementPadding * 2.0f, maxHeight) + skin.elementPadding * 2.0f
        contentHeight = areaHeight - skin.elementPadding * 2.0f

        if (scroll.y < 0.0f)
            scroll.y = 0.0f

        if (scroll.y > content.height - contentHeight)
            scroll.y = content.height - contentHeight
    }

    var totalWidth = areaWidth
    var totalHeight = areaHeight

    val rectangle = Rectangle()
    rectangle.x = x
    rectangle.y = y
    rectangle.width = totalWidth
    rectangle.height = totalHeight

    if (GUI.State.HOVERED in getState(rectangle)) {
        if (maxWidth != null) scroll.x += currentScrollAmount.x
        if (maxHeight != null) scroll.y += currentScrollAmount.y
    }

    val scrollbarCommands = GUICommandList()

    if (areaWidth - skin.elementPadding * 2.0f < content.width) {
        val scrollbarX = x
        val scrollbarY = y + contentHeight + skin.elementPadding * 2.0f
        val scrollbarWidth = areaWidth
        val scrollbarHeight = skin.scrollbarSize

        val scrollbarGripX = scrollbarX + scroll.x * contentWidth / content.width
        val scrollbarGripWidth = (contentWidth / content.width) * scrollbarWidth + skin.elementPadding

        val scrollbarState = getState(rectangle.apply {
            this.x = scrollbarX
            this.y = scrollbarY
            this.width = scrollbarWidth
            this.height = scrollbarHeight
        }, GUI.TouchBehaviour.NONE)

        val scrollbarGripState = getState(rectangle.apply {
            this.x = scrollbarGripX
            this.y = scrollbarY
            this.width = scrollbarGripWidth
            this.height = scrollbarHeight
        }, GUI.TouchBehaviour.REPEATED)

        val scrollbarColor = if (GUI.State.HOVERED in scrollbarState) skin.hoverColor else skin.normalColor

        val scrollbarGripColor = if (GUI.State.ACTIVE in scrollbarGripState && GUI.State.HOVERED in scrollbarGripState) {
            scroll.x += Kore.input.deltaX * content.width / contentWidth
            skin.highlightColor
        } else if (GUI.State.HOVERED in scrollbarGripState)
            skin.normalColor
        else
            skin.backgroundColor

        scrollbarCommands.drawRectFilled(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, skin.roundedCorners, skin.cornerRounding, scrollbarColor)
        scrollbarCommands.drawRectFilled(scrollbarGripX, scrollbarY, scrollbarGripWidth, scrollbarHeight, skin.roundedCorners, skin.cornerRounding, scrollbarGripColor)

        totalHeight += scrollbarHeight
    } else
        scroll.x = 0.0f

    if (areaHeight - skin.elementPadding * 2.0f < content.height) {
        val scrollbarX = x + contentWidth + skin.elementPadding * 2.0f
        val scrollbarY = y
        val scrollbarWidth = skin.scrollbarSize
        val scrollbarHeight = areaHeight

        val scrollbarGripY = scrollbarY + scroll.y * contentHeight / content.height
        val scrollbarGripHeight = (contentHeight / content.height) * scrollbarHeight + skin.elementPadding

        val scrollbarState = getState(rectangle.apply {
            this.x = scrollbarX
            this.y = scrollbarY
            this.width = scrollbarWidth
            this.height = scrollbarHeight
        }, GUI.TouchBehaviour.NONE)

        val scrollbarGripState = getState(rectangle.apply {
            this.x = scrollbarX
            this.y = scrollbarGripY
            this.width = scrollbarWidth
            this.height = scrollbarGripHeight
        }, GUI.TouchBehaviour.REPEATED)

        val scrollbarColor = if (GUI.State.HOVERED in scrollbarState) skin.hoverColor else skin.normalColor

        val scrollbarGripColor = if (GUI.State.ACTIVE in scrollbarGripState && GUI.State.HOVERED in scrollbarGripState) {
            scroll.y -= Kore.input.deltaY * content.height / contentHeight
            skin.highlightColor
        } else if (GUI.State.HOVERED in scrollbarGripState)
            skin.normalColor
        else
            skin.backgroundColor

        scrollbarCommands.drawRectFilled(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, skin.roundedCorners, skin.cornerRounding, scrollbarColor)
        scrollbarCommands.drawRectFilled(scrollbarX, scrollbarGripY, scrollbarWidth, scrollbarGripHeight, skin.roundedCorners, skin.cornerRounding, scrollbarGripColor)

        totalWidth += scrollbarWidth
    } else
        scroll.y = 0.0f

    currentCommandList.pushScissor(contentX, contentY, contentWidth, contentHeight)
    currentCommandList.addCommandList(commands)
    currentCommandList.popScissor()

    if (!scrollbarCommands.isEmpty)
        currentCommandList.addCommandList(scrollbarCommands)

    return setLastElement(x, y, totalWidth, totalHeight)
}
