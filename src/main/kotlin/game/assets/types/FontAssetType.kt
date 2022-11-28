package game.assets.types

import com.cozmicgames.*
import com.cozmicgames.files.FileHandle
import engine.Game
import engine.graphics.ui.DragDropData
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.widgets.image
import engine.graphics.ui.widgets.label
import game.assets.AssetType
import game.extensions.importButton
import game.level.editorStyle

class FontAssetType : AssetType<FontAssetType> {
    inner class FontImportPopup : SimpleImportPopup(this, "Import font") {
        override fun onImport(file: FileHandle) {
            Game.fonts.add(file)
        }
    }

    class FontAsset(val name: String)

    override val name = AssetTypes.FONTS

    override val iconName = "assets/images/assettype_font.png"

    override val assetNames get() = Game.fonts.names

    private val importPopup = FontImportPopup()

    override fun preview(gui: GUI, size: Float, name: String) {
        gui.image(Game.textures["assets/images/assettype_font.png"], size)
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
}