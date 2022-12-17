package game.assets.types

import com.cozmicgames.*
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.nameWithExtension
import com.cozmicgames.utils.Color
import engine.Game
import engine.graphics.asRegion
import engine.graphics.ui.DragDropData
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.widgets.image
import engine.graphics.ui.widgets.label
import game.assets.AssetType
import game.assets.MetaFile
import game.extensions.MENUOPTION_DELETE
import game.extensions.elementMenu
import game.extensions.importButton
import game.level.ui.editorStyle

class SoundAssetType : AssetType<SoundAssetType> {
    inner class SoundImportPopup : SimpleImportPopup(this, "Import sound") {
        override fun onImport(file: FileHandle, name: String) {
            Game.sounds.add(file, name)
        }
    }

    class SoundAsset(val name: String)

    override val name = AssetTypes.SOUNDS

    override val iconName = "internal/images/assettype_sound.png"

    override val supportedFormats get() = Kore.audio.supportedSoundFormats.toList()

    override val assetNames get() = Game.sounds.names

    private val importPopup = SoundImportPopup()

    override fun preview(gui: GUI, size: Float, name: String, showMenu: Boolean) {
        if (showMenu)
            gui.elementMenu({
                gui.image(Game.textures["internal/images/assettype_sound.png"] ?: Game.graphics2d.missingTexture.asRegion(), size)
            }, gui.skin.elementSize * 0.66f, arrayOf(MENUOPTION_DELETE), backgroundColor = Color.DARK_GRAY) {
                if (it == MENUOPTION_DELETE)
                    Game.sounds.remove(name)
            }
        else
            gui.image(Game.textures["internal/images/assettype_sound.png"] ?: Game.graphics2d.missingTexture.asRegion(), size)
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

    override fun load(file: FileHandle) {
        val metaFileHandle = file.sibling("${file.nameWithExtension}.meta")

        val name = if (metaFileHandle.exists) {
            val metaFile = MetaFile()
            metaFile.read(metaFileHandle)
            metaFile.name
        } else
            file.fullPath

        Game.sounds.add(file, name)
    }
}