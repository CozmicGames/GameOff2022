package game.level

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import engine.Game
import engine.graphics.ui.TextData
import engine.graphics.ui.widgets.*
import game.extensions.materialPreview

class MaterialEditor {
    private val colorTextData = TextData {
        try {
            val color = Color.fromHexString(text)
            materialName?.let {
                Game.materials[it]?.color?.set(color)
            }
        } catch (_: Exception) {

        }
    }

    private var materialName: String? = null

    var width: Float = 0.0f
        private set

    fun onFrame(materialName: String) {
        val material = Game.materials[materialName] ?: return

        if (materialName != this.materialName) {
            colorTextData.setText(material.color.toHexString())
            this.materialName = materialName
        }

        var width = 0.0f
        val editor = {
            Game.gui.group {
                Game.gui.label("Material", Game.editorStyle.panelTitleBackgroundColor, minWidth = width)
                Game.gui.materialPreview(material, Kore.graphics.width * Game.editorStyle.materialEditorPreviewSize)
                Game.gui.separator(width)

                Game.gui.label("Texture", Game.editorStyle.panelTitleBackgroundColor, minWidth = width)
                Game.gui.droppable<AssetSelector.TextureAsset>({ material.colorTexturePath = it.name }, 2.0f) {
                    Game.gui.tooltip(Game.gui.label(material.colorTexturePath, null, maxWidth = width), material.colorTexturePath)
                }
                Game.gui.separator(width)

                Game.gui.label("Shader", Game.editorStyle.panelTitleBackgroundColor, minWidth = width)
                Game.gui.droppable<AssetSelector.ShaderAsset>({ material.shader = it.name }) {
                    Game.gui.tooltip(Game.gui.label(material.shader, null, maxWidth = width), material.shader)
                }
                Game.gui.separator(width)

                Game.gui.label("Color", Game.editorStyle.panelTitleBackgroundColor, minWidth = width)

                Game.gui.colorEdit(material.color) {
                    colorTextData.setText(material.color.toHexString())
                }

                Game.gui.sameLine {
                    Game.gui.label("Hex: ", null)
                    Game.gui.textField(colorTextData)
                }
            }
        }

        width = Game.gui.getElementSize(editor).width
        editor()

        this.width = width
    }
}