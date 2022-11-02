package engine.graphics.ui

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.graphics.safeHeight
import com.cozmicgames.graphics.safeWidth
import com.cozmicgames.input
import com.cozmicgames.input.*
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Time
import com.cozmicgames.utils.maths.Matrix4x4
import com.cozmicgames.utils.maths.Rectangle
import com.cozmicgames.utils.maths.Vector2
import engine.Game
import engine.graphics.font.BitmapFont
import engine.graphics.shaders.DefaultShader
import kotlin.math.max

class GUI(val skin: GUISkin = GUISkin()) : Disposable {
    /**
     * The state an element can have.
     * HOVERED: The mouse is hovering over the element.
     * ACTIVE: The element is being interacted with.
     * ENTERED: The element has been entered.
     * LEFT: The element has been left.
     *
     * States can be combined to a bitfield using the combine function.
     * The resulting bitfield can be used to check if a state is active by the isSet function.
     */
    enum class State {
        HOVERED,
        ACTIVE,
        ENTERED,
        LEFT;

        companion object {
            /**
             * Combines the given states into a bitfield.
             * @param states The states to combine.
             * @param base The base state.
             *
             * @return The resulting bitfield.
             */
            fun combine(vararg states: State, base: Int = 0): Int {
                var flags = base
                states.forEach {
                    flags = flags or (1 shl it.ordinal)
                }
                return flags
            }

            /**
             * Checks if the given state is set in the bitfield.
             * @param flags The bitfield to check.
             * @param state The state to check for.
             *
             * @return True if the state is set, false otherwise.
             */
            fun isSet(flags: Int, state: State) = (flags and (1 shl state.ordinal)) != 0
        }
    }

    /**
     * Used for state bitfield generation.
     * @param state The state to add to the bitfield.
     *
     * @return The resulting bitfield.
     */
    operator fun Int.plus(state: State) = State.combine(state, base = this)

    /**
     * Used for checking if a state bitfield contains the specified state.
     * @param state The state to check for.
     *
     * @return True if the state is set, false otherwise.
     */
    operator fun Int.contains(state: State) = State.isSet(this, state)

    /**
     * Describes which behaviour an element has.
     * NONE: The element has no behaviour.
     * DEFAULT: The element is active once on interaction.
     * REPEATED: The element is active as long as it is interacted with.
     */
    enum class ButtonBehaviour {
        NONE,
        DEFAULT,
        REPEATED
    }

    /**
     * Describes the supported plot types.
     * POINTS: The plot is a series of points.
     * BARS: The plot is a series of bars.
     * LINES: The plot is a series of lines.
     */
    enum class PlotType {
        POINTS,
        BARS,
        LINES
    }

    private val commandList = GUICommandList()
    private val transform = Matrix4x4()
    private var isSameLine = false
    private var lineHeight = 0.0f
    private var tooltipCounter = 0.0f
    private var lastTime = Time.current

    private val inputListener = object : InputListener {
        override fun onKey(key: Key, down: Boolean, time: Double) {
            if (down)
                currentTextData?.onKeyAction(key)
        }

        override fun onChar(char: Char, time: Double) {
            currentTextData?.onCharAction(char)
        }

        override fun onScroll(x: Float, y: Float, time: Double) {
            currentScrollAmount.x -= x * skin.scrollSpeed
            currentScrollAmount.y -= y * skin.scrollSpeed
        }
    }

    private val layers = arrayListOf<GUILayer>()
    private var currentLayerIndex = 0

    /**
     * The last element that was added to the GUI.
     */
    var lastElement: GUIElement? = null
        private set

    /**
     * The current layer.
     */
    val currentLayer get() = layers[currentLayerIndex]

    /**
     * The current command list.
     */
    var currentCommandList = commandList
        private set

    /**
     * The current group, if one is present.
     * @see GUIGroup
     */
    var currentGroup: GUIGroup? = null
        private set

    /**
     * The current scissor rectangle, if one is present.
     */
    var currentScissorRectangle: Rectangle? = null

    /**
     * The current text data, if one is present.
     */
    var currentTextData: TextData? = null

    /**
     * The current combobox data, if one is present.
     */
    var currentComboBoxData: ComboboxData<*>? = null

    /**
     * The current touch position.
     */
    val touchPosition = Vector2()
        get() = field.set(Kore.input.x.toFloat(), (Kore.graphics.height - Kore.input.y).toFloat())

    /**
     * The previous frames' touch position.
     */
    val lastTouchPosition = Vector2()
        get() = field.set(Kore.input.lastX.toFloat(), (Kore.graphics.height - Kore.input.lastY).toFloat())

    /**
     * The current scroll amount.
     */
    val currentScrollAmount = Vector2()

    /**
     * The font used for text rendering.
     */
    val drawableFont = BitmapFont(skin.font, size = skin.contentSize)

    /**
     * Whether a tooltip should be shown, based on the time the pointer stands still.
     */
    val shouldShowTooltip get() = tooltipCounter >= skin.tooltipDelay

    init {
        Kore.input.addListener(inputListener)
        layers.add(GUILayer())
        currentCommandList = currentLayer.commands
    }

    /**
     * Executes [block] in a new layer on top of the current one.
     * Also sets the current command list for the time [block] runs to the new layer's command list.
     * Afterwards the current command list is set back to the previous one.
     *
     * @param block The block to execute.
     */
    fun layerUp(block: () -> Unit) {
        currentLayerIndex++
        if (currentLayerIndex >= layers.size)
            layers.add(GUILayer())

        val previousCommandList = currentCommandList
        currentCommandList = currentLayer.commands

        block()

        currentCommandList = previousCommandList
        currentLayerIndex--
    }

    /**
     * Executes [block] in a new layer below of the current one.
     * Also sets the current command list for the time [block] runs to the new layer's command list.
     * Afterwards the current command list is set back to the previous one.
     *
     * @param block The block to execute.
     */
    fun layerDown(block: () -> Unit) {
        currentLayerIndex--
        if (currentLayerIndex < 0) {
            currentLayerIndex = 0
            layers.add(0, GUILayer())
        }

        val previousCommandList = currentCommandList
        currentCommandList = currentLayer.commands

        block()

        currentCommandList = previousCommandList
        currentLayerIndex++
    }

    /**
     * Executes [block] on the top layer.
     * Also sets the current command list for the time [block] runs to the top layer's command list.
     * Afterwards the current command list and current layer is set back to the previous one.
     *
     * @param block The block to execute.
     */
    fun topLayer(block: () -> Unit) {
        val previousLayerIndex = currentLayerIndex
        currentLayerIndex = layers.size - 1

        val previousCommandList = currentCommandList
        currentCommandList = currentLayer.commands

        block()

        currentCommandList = previousCommandList
        currentLayerIndex = previousLayerIndex
    }

    /**
     * Executes [block] on the bottom layer.
     * Also sets the current command list for the time [block] runs to the bottom layer's command list.
     * Afterwards the current command list and current layer is set back to the previous one.
     *
     * @param block The block to execute.
     */
    fun bottomLayer(block: () -> Unit) {
        val previousLayerIndex = currentLayerIndex
        currentLayerIndex = 0

        val previousCommandList = currentCommandList
        currentCommandList = currentLayer.commands

        block()

        currentCommandList = previousCommandList
        currentLayerIndex = previousLayerIndex
    }

    /**
     * Records the commands added during execution of [block] and returns them as a command list.
     *
     * @param block The block to execute.
     *
     * @return The command list.
     */
    fun recordCommands(block: () -> Unit): GUICommandList {
        val list = GUICommandList()
        val previousList = currentCommandList
        currentCommandList = list
        block()
        currentCommandList = previousList
        return list
    }

    /**
     * Sets the last element.
     * This is used to determine the position of the next element.
     * This is also used to determine the size of the panel.
     *
     * All GUI functions must end with this and should return the element.
     *
     * @param x The x position of the element.
     * @param y The y position of the element.
     * @param width The width of the element.
     * @param height The height of the element.
     *
     * @return The element.
     */
    fun setLastElement(x: Float, y: Float, width: Float, height: Float): GUIElement {
        return setLastElement(
            if (isSameLine)
                object : GUIElement(x, y, width, height) {
                    override val nextX get() = x + width
                    override val nextY get() = y
                }
            else
                object : GUIElement(x, y, width, height) {
                    override val nextX get() = x
                    override val nextY get() = y + height
                }
        )
    }

    /**
     * Sets the last element.
     * This is used to determine the position of the next element.
     * This is also used to determine the size of the panel.
     *
     * All GUI functions must end with this and should return the element.
     *
     * @param element The element.
     *
     * @return The element.
     */
    fun setLastElement(element: GUIElement): GUIElement {
        currentLayer.addElement(element)

        lineHeight = if (isSameLine)
            max(lineHeight, element.height)
        else
            element.height

        lastElement = element

        currentGroup?.let {
            it.width = max(it.width, element.x + element.width - it.x + skin.elementPadding)

            if (!isSameLine)
                it.height += lineHeight
        }

        return element
    }

    /**
     * Gets the last element.
     * This is used to determine the position of the current element.
     *
     * @param defaultX The default x position of the element if no last element is set. Defaults to 0.0f.
     * @param defaultY The default y position of the element if no last element is set. Defaults to 0.0f.
     *
     * @return The last element.
     */
    fun getLastElement(defaultX: Float = 0.0f, defaultY: Float = 0.0f): GUIElement {
        return lastElement ?: absolute(defaultX, defaultY)
    }

    /**
     * Sets the last element to an absolute position.
     *
     * @param point The position.
     *
     * @return The created element.
     */
    fun absolute(point: Vector2) = absolute(point.x, point.y)

    /**
     * Sets the last element to an absolute position.
     *
     * @param x The x position.
     * @param y The y position.
     *
     * @return The created element.
     */
    fun absolute(x: Float, y: Float) = object : GUIElement(x, y, 0.0f, 0.0f) {
        override val nextX get() = x
        override val nextY get() = y
    }

    /**
     * Sets the last element to a relative position.
     * It's calculated by x = factorX * srcX and y = factorY * srcY.
     *
     * @param factorX The x factor, should be between 0 and 1.
     * @param factorY The y factor, should be between 0 and 1.
     * @param srcX The source size in the x dimension. Defaults to the surface width.
     * @param srcY The source size in the y dimension. Defaults to the surface height.
     *
     * @return The created element.
     */
    fun relative(factorX: Float, factorY: Float, srcX: Float = Kore.graphics.width.toFloat(), srcY: Float = Kore.graphics.height.toFloat()) = object : GUIElement(factorX * srcX, factorY * srcY, 0.0f, 0.0f) {
        override val nextX get() = x
        override val nextY get() = y
    }

    /**
     * Sets the last element to an offset from [src].
     * It's calculated by x = srcX + offsetX and y = srcY + offsetY.
     *
     * @param offset The offset.
     * @param src The source element. Defaults to the last element.
     * @param resetX Whether to reset the x position afterwards. Defaults to false.
     * @param resetY Whether to reset the y position afterwards. Defaults to false.
     * @param block The block to execute.
     */
    fun offset(offset: GUIElement, src: GUIElement = getLastElement(), resetX: Boolean = false, resetY: Boolean = false, block: () -> Unit) = offset(offset.nextX, offset.nextY, src, resetX, resetY, block)

    /**
     * Sets the last element to an offset from [src].
     * It's calculated by x = srcX + offsetX and y = srcY + offsetY.
     *
     * @param offsetX The x offset.
     * @param offsetY The y offset.
     * @param src The source element. Defaults to the last element.
     * @param resetX Whether to reset the x position afterwards. Defaults to false.
     * @param resetY Whether to reset the y position afterwards. Defaults to false.
     * @param block The block to execute.
     */
    fun offset(offsetX: Float, offsetY: Float, src: GUIElement = getLastElement(), resetX: Boolean = false, resetY: Boolean = false, block: () -> Unit) = offset(offsetX, offsetY, src.nextX, src.nextY, resetX, resetY, block)

    /**
     * Sets the last element to an offset from [src].
     * It's calculated by x = srcX + offsetX and y = srcY + offsetY.
     *
     * @param offsetX The x offset.
     * @param offsetY The y offset.
     * @param srcX The source x position.
     * @param srcY The source y position.
     * @param resetX Whether to reset the x position afterwards. Defaults to false.
     * @param resetY Whether to reset the y position afterwards. Defaults to false.
     * @param block The block to execute.
     */
    fun offset(offsetX: Float, offsetY: Float, srcX: Float, srcY: Float, resetX: Boolean = false, resetY: Boolean = false, block: () -> Unit): GUIElement {
        val lastElement = getLastElement()
        setLastElement(srcX + offsetX, srcY + offsetY, 0.0f, 0.0f)
        block()
        return setLastElement(if (resetX) lastElement.nextX else getLastElement().nextX, if (resetY) lastElement.nextY else getLastElement().nextY, 0.0f, 0.0f)
    }

    /**
     * Performs a block of code and resetting the last element afterwards.
     *
     * @param block The block to execute.
     */
    fun transient(block: () -> Unit): GUIElement {
        val lastElement = getLastElement()
        block()
        return setLastElement(lastElement)
    }

    /**
     * Adds a blank line element.
     *
     * @param size The size of the blank line.
     */
    fun blankLine(size: Float = skin.elementSize): GUIElement {
        val (x, y) = getLastElement()
        return setLastElement(x, y, 0.0f, size)
    }

    /**
     * Adds a spacing element.
     * The spacing is a horizontal gap of the given [width].
     *
     * @param width The width of the spacing. Defaults to [skin.elementSize].
     */
    fun spacing(width: Float = skin.elementSize): GUIElement {
        val (x, y) = getLastElement()
        return setLastElement(x, y, width, 0.0f)
    }

    /**
     * Gets the state of the area covered by [rectangle] with the given [behaviour].
     *
     * @param rectangle The rectangle.
     * @param behaviour The behaviour. Defaults to [ButtonBehaviour.NONE].
     *
     * @return The state of the area as a bitfield.
     */
    fun getState(rectangle: Rectangle, behaviour: ButtonBehaviour = ButtonBehaviour.NONE): Int {
        var state = 0

        for (layerIndex in layers.indices.reversed()) {
            if (layerIndex == currentLayerIndex)
                break

            if (layers[layerIndex].contains(touchPosition.x, touchPosition.y))
                return state
        }

        currentScissorRectangle?.let {
            if (touchPosition !in it || !(it intersects rectangle))
                return state
        }

        if (touchPosition in rectangle) {
            state += State.HOVERED

            when (behaviour) {
                ButtonBehaviour.DEFAULT -> if (Kore.input.justTouchedUp) state += State.ACTIVE
                ButtonBehaviour.REPEATED -> if (Kore.input.isTouched) state += State.ACTIVE
                else -> {}
            }

            if (lastTouchPosition !in rectangle)
                state += State.ENTERED
        } else if (lastTouchPosition in rectangle)
            state += State.LEFT

        return state
    }

    /**
     * Marks the elements added during the execution of [block] to be laid out horizontally.
     * The elements will be laid out from left to right.
     *
     * @param block The block to execute.
     */
    fun sameLine(block: () -> Unit): GUIElement {
        if (isSameLine) {
            block()
            return getLastElement()
        }

        val lastElement = getLastElement()
        isSameLine = true
        lineHeight = 0.0f

        block()

        isSameLine = false

        return setLastElement(lastElement.nextX, lastElement.nextY, getLastElement().nextX - lastElement.nextX, lineHeight)
    }

    /**
     * Groups the elements added during the execution of [block] and sets the last element as a union of those.
     * This is useful for grouping elements that are not laid out horizontally.
     *
     * @param block The block to execute.
     */
    fun group(backgroundColor: Color? = null, block: () -> Unit): GUIElement {
        val (x, y) = getLastElement()

        val previousGroup = currentGroup
        val previousSameLine = isSameLine
        val previousLineHeight = lineHeight

        val group = GUIGroup(x, y, 0.0f, 0.0f)
        currentGroup = group
        isSameLine = false
        lineHeight = 0.0f

        val commands = recordCommands(block)

        backgroundColor?.let {
            currentCommandList.drawRectFilled(group.x, group.y, group.width, group.height, skin.roundedCorners, skin.cornerRounding, it)
        }
        currentCommandList.addCommandList(commands)

        currentGroup = previousGroup
        isSameLine = previousSameLine
        lineHeight = previousLineHeight

        return setLastElement(group.x, group.y, group.width, group.height)
    }

    /**
     * Begin GUI rendering.
     * This must be called before any widget function.
     */
    fun begin() {
        lastElement = null

        val currentTime = Time.current
        val deltaTime = (currentTime - lastTime).toFloat()
        lastTime = currentTime

        if (touchPosition.x == lastTouchPosition.x && touchPosition.y == lastTouchPosition.y)
            tooltipCounter += deltaTime
        else
            tooltipCounter = 0.0f
    }

    /**
     * End GUI rendering.
     * This must be called after all widget functions.
     * It will render the GUI by flushing the command list.
     * It will also reset the command list.
     */
    fun end() {
        currentScrollAmount.mul(0.9f)
        if (currentScrollAmount.lengthSquared > 0.01f)
            currentScrollAmount.setZero()

        transform.setToOrtho2D(Kore.graphics.safeInsetLeft.toFloat(), Kore.graphics.safeWidth.toFloat(), Kore.graphics.safeHeight.toFloat(), Kore.graphics.safeInsetTop.toFloat())

        Game.graphics2d.render(transform) { renderer ->
            renderer.withTransientState {
                flipX = false
                flipY = true
                shader = DefaultShader
                layers.forEach {
                    it.process(renderer)
                }
            }
        }
    }

    /**
     * Disposes the GUI and all its resources.
     */
    override fun dispose() {
        Kore.input.removeListener(inputListener)
        drawableFont.dispose()
    }
}
