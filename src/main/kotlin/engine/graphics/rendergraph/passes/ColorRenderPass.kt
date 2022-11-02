package engine.graphics.render.passes

import com.cozmicgames.graphics.gpu.Texture
import engine.graphics.render.RenderPass
import engine.graphics.render.standardResolution

class ColorRenderPass(resolution: Resolution = standardResolution(), colorFormat: Texture.Format = Texture.Format.RGBA8_UNORM) : RenderPass(resolution) {
    val color = addColorRenderTarget(colorFormat)
}