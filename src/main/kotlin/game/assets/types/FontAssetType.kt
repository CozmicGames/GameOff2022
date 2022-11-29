package game.assets.types

import com.cozmicgames.*
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.nameWithoutExtension
import engine.Game
import engine.graphics.ui.DragDropData
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.widgets.image
import engine.graphics.ui.widgets.label
import game.assets.AssetType
import game.assets.MetaFile
import game.extensions.importButton
import game.level.editorStyle

class FontAssetType : AssetType<FontAssetType> {
    inner class FontImportPopup : SimpleImportPopup(this, "Import font") {
        override fun onImport(file: FileHandle, name: String) {
            Game.fonts.add(file, name)
        }
    }

    class FontAsset(val name: String)

    override val name = AssetTypes.FONTS

    override val iconName = "internal/images/assettype_font.png"

    override val supportedFormats get() = Kore.graphics.supportedFontFormats.toList()

    override val assetNames get() = Game.fonts.names

    private val importPopup = FontImportPopup()

    override fun preview(gui: GUI, size: Float, name: String) {
        gui.image(Game.textures["internal/images/assettype_font.png"], size)
    }

    override fun createDragDropData(name: String) = { DragDropData(FontAsset(name)) { label(name) } }

    override fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>) {
        list += {
            gui.importButton(Game.editorStyle.assetElementWidth) {
                Kore.dialogs.open("Open file", filters = Kore.graphics.supportedFontFormats.toList().toTypedArray())?.let {
                    importPopup.reset(it)
                    gui.popup(importPopup)
                }
            }
        }
    }

    override fun load(file: FileHandle) {
        val metaFileHandle = file.sibling("${file.nameWithoutExtension}.meta")

        val name = if (metaFileHandle.exists) {
            val metaFile = MetaFile()
            metaFile.read(metaFileHandle)
            metaFile.name
        } else
            file.fullPath

        Game.fonts.add(file, name)
    }
}