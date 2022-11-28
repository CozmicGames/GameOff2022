package game.assets.types

import com.cozmicgames.*
import com.cozmicgames.files.FileHandle
import engine.Game
import engine.graphics.ui.DragDropData
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.widgets.label
import engine.materials.Material
import game.assets.AssetType
import game.extensions.importButton
import game.extensions.materialPreview
import game.extensions.plusButton
import game.level.editorStyle

class MaterialAssetType : AssetType<MaterialAssetType> {
    inner class MaterialImportPopup : SimpleImportPopup(this, "Import material") {
        override fun onImport(file: FileHandle) {
            Game.materials.add(file)
        }
    }

    class MaterialAsset(val name: String)

    override val name = AssetTypes.MATERIALS

    override val iconName = "assets/images/assettype_material.png"

    override val assetNames get() = Game.materials.names

    private val createFilePopup = CreateFilePopup()
    private val importPopup = MaterialImportPopup()

    override fun preview(gui: GUI, size: Float, name: String) {
        gui.materialPreview(Game.materials[name] ?: Game.graphics2d.missingMaterial, size)
    }

    override fun createDragDropData(name: String) = { DragDropData(MaterialAsset(name)) { label(name) } }

    override fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>) {
        list += {
            gui.plusButton(Game.editorStyle.assetElementWidth) {
                createFilePopup.reset {
                    Game.materials.add(it, Material())
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
}