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

class SoundAssetType : AssetType<SoundAssetType> {
    inner class SoundImportPopup : SimpleImportPopup(this, "Import sound") {
        override fun onImport(file: FileHandle) {
            Game.sounds.add(file)
        }
    }

    class SoundAsset(val name: String)

    override val name = AssetTypes.SOUNDS

    override val iconName = "assets/images/assettype_sound.png"

    override val assetNames get() = Game.sounds.names

    private val importPopup = SoundImportPopup()

    override fun preview(gui: GUI, size: Float, name: String) {
        gui.image(Game.textures["assets/images/assettype_sound.png"], size)
    }

    override fun createDragDropData(name: String) = { DragDropData(SoundAsset(name)) { label(name) } }

    override fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>) {
        list += {
            gui.importButton(Game.editorStyle.assetElementWidth) {
                Kore.dialogs.open("Open file", filters = Kore.audio.supportedSoundFormats.toList().toTypedArray())?.let {
                    importPopup.reset(it)
                    gui.popup(importPopup)
                }
            }
        }
    }
}