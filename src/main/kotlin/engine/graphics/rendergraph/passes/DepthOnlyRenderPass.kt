package engine.graphics.render.passes

import com.cozmicgames.graphics.gpu.Texture
import engine.graphics.render.RenderPass
import engine.graphics.render.addDepthRenderTarget
import engine.graphics.render.standardResolution

class DepthOnlyRenderPass(resolution: Resolution = standardResolution(), depthFormat: Texture.Format = Texture.Format.DEPTH24) : RenderPass(resolution) {
    val depth = addDepthRenderTarget(depthFormat)
}