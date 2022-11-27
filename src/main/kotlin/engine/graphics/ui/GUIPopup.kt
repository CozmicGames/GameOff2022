package engine.graphics.ui

abstract class GUIPopup {
    var isActive = true
        private set

    fun closePopup() {
        isActive = false
    }

    abstract fun draw(gui: GUI, width: Float, height: Float): GUIElement
}
