package engine.graphics.ui

class GUIPopup(val draw: GUIPopup.(GUI, Float, Float) -> GUIElement) {
    var isActive = true
        private set

    fun closePopup() {
        isActive = false
    }
}