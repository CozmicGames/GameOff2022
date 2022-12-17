package game.assets.managers

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.log
import engine.graphics.shaders.DefaultShader
import engine.graphics.shaders.Shader

class ShaderManager : StandardAssetTypeManager<Shader, Unit>() {
    override val defaultParams = Unit

    override val defaultValue = DefaultShader

    init {
        add("default", DefaultShader)
    }

    override fun add(file: FileHandle, name: String, params: Unit) {
        if (!file.exists) {
            Kore.log.error(this::class, "Shader file not found: $file")
            return
        }

        val shader = try {
            Shader(file)
        } catch (e: Exception) {
            Kore.log.error(this::class, "Failed to load shader file: $file")
            return
        }

        add(name, shader, file)
    }
}