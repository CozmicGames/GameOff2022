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
import game.extensions.plusButton
import game.level.TileSet
import game.level.editorStyle

class TileSetAssetType : AssetType<TileSetAssetType> {
    inner class TileSetImportPopup : SimpleImportPopup(this, "Import tileset") {
        override fun onImport(file: FileHandle, name: String) {
            Game.tileSets.add(file, name)
        }
    }

    class TileSetAsset(val name: String)

    override val name = AssetTypes.TILESETS

    override val iconName = "internal/images/assettype_tileset.png"

    override val supportedFormats = listOf("tileset")

    override val assetNames get() = Game.tileSets.names

    private val createFilePopup = CreateFilePopup()
    private val importPopup = TileSetImportPopup()

    override fun preview(gui: GUI, size: Float, name: String) {
        gui.image(Game.textures["internal/images/assettype_tileset.png"], size)
    }

    override fun createDragDropData(name: String) = { DragDropData(TileSetAsset(name)) { label(name) } }

    override fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>) {
        list += {
            gui.plusButton(Game.editorStyle.assetElementWidth) {
                createFilePopup.reset {
                    Game.tileSets.add(it, TileSet())

                }
                gui.popup(createFilePopup)
            }
        }

        list += {
            gui.importButton(Game.editorStyle.assetElementWidth) {
                Kore.dialogs.open("Open file", filters = arrayOf("tileset"))?.let {
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

        Game.tileSets.add(file, name)
    }
}