package engine.scene.components

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.Files
import com.cozmicgames.files.readToString
import com.cozmicgames.files.writeString
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Properties
import engine.materials.Material
import engine.scene.Component

class MaterialComponent : Component(), Disposable {
    var file: String? = null
        set(value) {
            field = value
            reload()
        }

    var fileType = Files.Type.ASSET
        set(value) {
            field = value
            reload()
        }

    val material = Material()

    override fun onAdded() {
        reload()
    }

    private fun reload() {
        val file = this.file ?: return

        val fileHandle = when (fileType) {
            Files.Type.ASSET -> Kore.files.asset(file)
            Files.Type.ABSOLUTE -> Kore.files.absolute(file)
            Files.Type.EXTERNAL -> Kore.files.external(file)
            Files.Type.LOCAL -> Kore.files.local(file)
            else -> return
        }

        material.read(fileHandle.readToString())
    }

    override fun dispose() {
        val file = this.file ?: return

        val fileHandle = when (fileType) {
            Files.Type.ASSET -> Kore.files.asset(file)
            Files.Type.ABSOLUTE -> Kore.files.absolute(file)
            Files.Type.EXTERNAL -> Kore.files.external(file)
            Files.Type.LOCAL -> Kore.files.local(file)
            else -> return
        }

        fileHandle.writeString(material.write(), false)
    }

    override fun read(properties: Properties) {
        properties.getString("file")?.let { file = it }
        properties.getString("fileType")?.let { fileType = enumValueOf(it) }
    }

    override fun write(properties: Properties) {
        properties.setString("file", file ?: "")
        properties.setString("fileType", fileType.toString())
    }
}
