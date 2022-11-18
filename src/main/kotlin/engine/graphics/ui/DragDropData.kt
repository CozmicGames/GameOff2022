package engine.graphics.ui

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import engine.graphics.ui.widgets.label

open class DragDropData<T : Any>(val payload: T, val onDrawPayload: GUI.() -> Unit) {
    private var renderedFrame = -1

    fun drawPayload(gui: GUI) {
        if (renderedFrame == Kore.graphics.statistics.numFrames)
            return

        gui.topLayer {
            gui.transient(true, false) {
                gui.setLastElement(gui.absolute(gui.touchPosition))
                onDrawPayload(gui)
            }
        }

        renderedFrame = Kore.graphics.statistics.numFrames
    }
}

class StringDragDropData(payload: String) : DragDropData<String>(payload, { label(payload, skin.hoverColor) })
