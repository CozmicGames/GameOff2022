package engine.engine.graphics.render.passes

import com.cozmicgames.graphics.gpu.Texture
import engine.graphics.render.RenderPass
import engine.graphics.render.addDepthRenderTarget
import engine.graphics.render.standardResolution

class MultiColorDepthRenderPass(resolution: Resolution = standardResolution(), colorFormats: Array<Texture.Format> = arrayOf(Texture.Format.RGBA8_UNORM), depthFormat: Texture.Format = Texture.Format.DEPTH24) : RenderPass(resolution) {
    val colors = Array(colorFormats.size) {
        addColorRenderTarget(colorFormats[it])
    }
    val depth = addDepthRenderTarget(depthFormat)
}