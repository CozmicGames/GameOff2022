package game.assets.managers

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.readToString
import com.cozmicgames.log
import engine.graphics.Material

class MaterialManager : StandardAssetTypeManager<Material, Unit>() {
    override val defaultParams = Unit

    override fun add(file: FileHandle, name: String, params: Unit) {
        if (!file.exists) {
            Kore.log.error(this::class, "Material file not found: $file")
            return
        }

        val material = try {
            Material().also {
                it.read(file.readToString())
            }
        } catch (e: Exception) {
            Kore.log.error(this::class, "Failed to load material file: $file")
            return
        }

        add(name, material, file)
    }
}