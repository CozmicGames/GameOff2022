package engine.graphics.ui

abstract class GUIElement(val x: Float, val y: Float, val width: Float, val height: Float) {
    abstract val nextX: Float
    abstract val nextY: Float

    operator fun component1() = nextX
    operator fun component2() = nextY
}
