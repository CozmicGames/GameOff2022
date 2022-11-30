package game.assets.types

import com.cozmicgames.*
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.nameWithExtension
import engine.Game
import engine.graphics.ui.DragDropData
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.widgets.image
import engine.graphics.ui.widgets.label
import game.assets.AssetType
import game.assets.MetaFile
import game.extensions.importButton
import game.level.ui.editorStyle

class ShaderAssetType : AssetType<ShaderAssetType> {
    inner class ShaderImportPopup : SimpleImportPopup(this, "Import shader") {
        override fun onImport(file: FileHandle, name: String) {
            Game.shaders.add(file, name)
        }
    }

    class ShaderAsset(val name: String)

    override val name = AssetTypes.SHADERS

    override val iconName = "internal/images/assettype_shader.png"

    override val supportedFormats = listOf("shader")

    override val assetNames get() = Game.shaders.names

    private val importPopup = ShaderImportPopup()

    override fun preview(gui: GUI, size: Float, name: String, showEditIcon: Boolean) {
        gui.image(Game.textures["internal/images/assettype_shader.png"], size)
    }

    override fun createDragDropData(name: String) = { DragDropData(ShaderAsset(name)) { label(name) } }

    override fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>) {
        list += {
            gui.importButton(Game.editorStyle.assetElementWidth) {
                Kore.dialogs.open("Open file", filters = arrayOf("shader"))?.let {
                    importPopup.reset(it)
                    gui.popup(importPopup)
                }
            }
        }
    }

    override fun load(file: FileHandle) {
        val metaFileHandle = file.sibling("${file.nameWithExtension}.meta")

        val name = if (metaFileHandle.exists) {
            val metaFile = MetaFile()
            metaFile.read(metaFileHandle)
            metaFile.name
        } else
            file.fullPath

        Game.shaders.add(file, name)
    }
}