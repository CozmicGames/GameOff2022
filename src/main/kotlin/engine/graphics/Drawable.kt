package engine.graphics

interface Drawable {
    data class Vertex(var x: Float = 0.0f, var y: Float = 0.0f, var u: Float = 0.0f, var v: Float = 0.0f)

    val vertices: Array<Vertex>
    val indices: Array<Int>

    val verticesCount get() = vertices.size
    val indicesCount get() = indices.size
}