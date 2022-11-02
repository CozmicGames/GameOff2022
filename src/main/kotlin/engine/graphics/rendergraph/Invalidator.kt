package engine.graphics.render

class Invalidator {
    internal var node: RenderGraph.Node.OnInvalid? = null

    fun invalidate() {
        node?.setDirty()
    }
}