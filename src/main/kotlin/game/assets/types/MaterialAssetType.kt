package game.assets.types

import com.cozmicgames.*
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.nameWithExtension
import com.cozmicgames.files.writeString
import com.cozmicgames.utils.Color
import engine.Game
import engine.graphics.ui.DragDropData
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.widgets.label
import engine.graphics.Material
import game.assets.AssetType
import game.assets.MetaFile
import game.extensions.*
import game.level.ui.MaterialEditorPopup
import game.level.ui.editorStyle

class MaterialAssetType : AssetType<MaterialAssetType> {
    inner class MaterialImportPopup : SimpleImportPopup(this, "Import material") {
        override fun onImport(file: FileHandle, name: String) {
            Game.materials.add(file, name)
        }
    }

    class MaterialAsset(val name: String)

    override val name = AssetTypes.MATERIALS

    override val iconName = "internal/images/assettype_material.png"

    override val supportedFormats = listOf("material")

    override val assetNames get() = Game.materials.names

    private val createFilePopup = CreateFilePopup("material")
    private val importPopup = MaterialImportPopup()
    private val editorPopup = MaterialEditorPopup()

    override fun preview(gui: GUI, size: Float, name: String, showMenu: Boolean) {
        if (showMenu) {
            val options = if (Game.materials.getFileHandle(name)?.isWritable != false) arrayOf(MENUOPTION_EDIT, MENUOPTION_DELETE) else arrayOf(MENUOPTION_DELETE)

            gui.elementMenu({
                gui.materialPreview(Game.materials[name] ?: Game.graphics2d.missingMaterial, size)
            }, gui.skin.elementSize * 0.66f, options, backgroundColor = Color.DARK_GRAY) {
                when (it) {
                    MENUOPTION_EDIT -> Kore.onNextFrame {
                        editorPopup.reset(name)
                        gui.popup(editorPopup)
                    }
                    MENUOPTION_DELETE -> Game.materials.remove(name)
                }
            }
        } else
            gui.materialPreview(Game.materials[name] ?: Game.graphics2d.missingMaterial, size)
    }

    override fun createDragDropData(name: String) = { DragDropData(MaterialAsset(name)) { label(name) } }

    override fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>) {
        list += {
            gui.plusButton(Game.editorStyle.assetElementWidth) {
                createFilePopup.reset {
                    val assetFile = Kore.files.local("assets/$it")
                    assetFile.writeString(Material().write(true), false)
                    Game.materials.add(it, Material(), assetFile)
                }
                gui.popup(createFilePopup)
            }
        }

        list += {
            gui.importButton(Game.editorStyle.assetElementWidth) {
                Kore.dialogs.open("Open file", filters = arrayOf("material"))?.let {
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

        Game.materials.add(file, name)
    }
}