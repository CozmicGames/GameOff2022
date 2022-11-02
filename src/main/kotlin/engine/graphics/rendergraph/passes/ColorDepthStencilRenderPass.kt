package engine.graphics.render.passes

import com.cozmicgames.graphics.gpu.Texture
import engine.graphics.render.RenderPass
import engine.graphics.render.addDepthRenderTarget
import engine.graphics.render.addStencilRenderTarget
import engine.graphics.render.standardResolution

class ColorDepthStencilRenderPass(resolution: Resolution = standardResolution(), colorFormat: Texture.Format = Texture.Format.RGBA8_UNORM, depthFormat: Texture.Format = Texture.Format.DEPTH24, stencilFormat: Texture.Format = Texture.Format.STENCIL8) : RenderPass(resolution) {
    val color = addColorRenderTarget(colorFormat)
    val depth = addDepthRenderTarget(depthFormat)
    val stencil = addStencilRenderTarget(stencilFormat)
}