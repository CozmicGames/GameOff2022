package game.assets.types

import com.cozmicgames.*
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.nameWithExtension
import com.cozmicgames.files.writeString
import com.cozmicgames.utils.Properties
import engine.Game
import engine.graphics.ui.DragDropData
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.widgets.image
import engine.graphics.ui.widgets.label
import game.assets.AssetType
import game.assets.MetaFile
import game.extensions.editable
import game.extensions.importButton
import game.extensions.plusButton
import game.level.TileSet
import game.level.ui.TileSetEditorPopup
import game.level.ui.editorStyle

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

    private val createFilePopup = CreateFilePopup("tileset")
    private val importPopup = TileSetImportPopup()
    private val editorPopup = TileSetEditorPopup()

    override fun preview(gui: GUI, size: Float, name: String, showEditIcon: Boolean) {
        if (showEditIcon && Game.tileSets.getFileHandle(name)?.isWritable != false)
            gui.editable({ gui.image(Game.textures["internal/images/assettype_tileset.png"], size) }, size * 0.25f) {
                Kore.onNextFrame {
                    editorPopup.reset(name)
                    gui.popup(editorPopup)
                }
            }
        else
            gui.image(Game.textures["internal/images/assettype_tileset.png"], size)
    }

    override fun createDragDropData(name: String) = { DragDropData(TileSetAsset(name)) { label(name) } }

    override fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>) {
        list += {
            gui.plusButton(Game.editorStyle.assetElementWidth) {
                createFilePopup.reset {
                    val assetFile = Kore.files.local("assets/$it")
                    assetFile.writeString(Properties().also { TileSet(name).write(it) }.write(true), false)
                    Game.tileSets.add(it, TileSet(it), assetFile)
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
        val metaFileHandle = file.sibling("${file.nameWithExtension}.meta")

        val name = if (metaFileHandle.exists) {
            val metaFile = MetaFile()
            metaFile.read(metaFileHandle)
            metaFile.name
        } else
            file.fullPath

        Game.tileSets.add(file, name)
    }
}