package game.level

import com.cozmicgames.*
import com.cozmicgames.graphics.Image
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.gpu.Texture2D
import com.cozmicgames.graphics.split
import com.cozmicgames.graphics.toTexture2D
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.extensions.extension
import com.cozmicgames.utils.extensions.pathWithoutExtension
import com.cozmicgames.utils.maths.Vector2
import engine.Game
import engine.graphics.TextureRegion
import engine.graphics.asRegion
import engine.graphics.font.HAlign
import engine.graphics.ui.ComboboxData
import engine.graphics.ui.DragDropData
import engine.graphics.ui.TextData
import engine.graphics.ui.drawLine
import engine.graphics.ui.widgets.*
import game.extensions.downButton
import game.extensions.materialPreview
import game.extensions.plusButton
import game.extensions.upButton
import kotlin.math.max
import kotlin.math.min

class TileTypeEditor : Disposable {
    sealed interface ReturnState {
        object None : ReturnState

        object Menu : ReturnState

        object LevelEditor : ReturnState
    }

    private val assetSelector = AssetSelector()
    private val materialEditorScroll = Vector2()

    fun drawTitle(setReturnState: (ReturnState) -> Unit) {
        val label = {
            Game.gui.label("Edit Tileset", null)
        }

        val settingsButton = {
            Game.gui.textButton("Settings") {
                setReturnState(ReturnState.Menu)
            }
        }

        val backButton = {
            Game.gui.textButton("Back") {
                setReturnState(ReturnState.LevelEditor)
            }
        }

        val saveButton = {
            Game.gui.textButton("Save") {
                //TODO: Save something!
                setReturnState(ReturnState.LevelEditor)
            }
        }

        val labelWidth = Game.gui.getElementSize(label).width

        val buttonsWidth = Game.gui.getElementSize {
            Game.gui.sameLine {
                settingsButton()
                backButton()
                saveButton()
            }
        }.width

        Game.gui.group(Game.editorStyle.panelTitleBackgroundColor) {
            Game.gui.sameLine {
                label()
                Game.gui.spacing(Kore.graphics.width - labelWidth - buttonsWidth)
                settingsButton()
                backButton()
                saveButton()
            }
        }
    }

    fun drawMaterialEditor(materialName: String) {
        val material = Game.materials[materialName] ?: return

        var width = 0.0f

        val drawEditor = {
            //Game.gui.panel(width, Kore.graphics.height - Game.gui.skin.elementSize - Kore.graphics.height * Game.editorStyle.assetSelectionPanelHeight, materialEditorScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
            //    Game.gui.label("Material Editor", null)
            //}) {
            //}


            Game.gui.group(Game.editorStyle.panelContentBackgroundColor) {
                Game.gui.group(Game.editorStyle.panelTitleBackgroundColor, width) { Game.gui.label("Material", null) }
                Game.gui.materialPreview(material, Kore.graphics.width * Game.editorStyle.materialEditorPreviewSize)
                Game.gui.separator(width)

                Game.gui.group(Game.editorStyle.panelTitleBackgroundColor, width) { Game.gui.label("Texture", null) }
                Game.gui.droppable<AssetSelector.TextureAsset>({ material.colorTexturePath = it.name }, 2.0f) {
                    Game.gui.tooltip(Game.gui.label(material.colorTexturePath, null, maxWidth = width), material.colorTexturePath)
                }
                Game.gui.separator(width)

                Game.gui.group(Game.editorStyle.panelTitleBackgroundColor, width) { Game.gui.label("Shader", null) }
                Game.gui.droppable<AssetSelector.ShaderAsset>({ material.shader = it.name }) {
                    Game.gui.tooltip(Game.gui.label(material.shader, null, maxWidth = width), material.shader)
                }
                Game.gui.separator(width)

                Game.gui.group(Game.editorStyle.panelTitleBackgroundColor, width) { Game.gui.label("Color", null) }
                Game.gui.colorEdit(material.color)
            }
        }

        width = Game.gui.getElementSize(drawEditor).width

        Game.gui.setLastElement(Game.gui.absolute(Kore.graphics.width - width, Game.gui.skin.elementSize))
        drawEditor()
    }

    fun onFrame(editTileTypeName: String, tileSetName: String): ReturnState {
        var returnState: ReturnState = ReturnState.None

        if (tileSetName !in Game.tileSets)
            return returnState

        val tileSet = Game.tileSets[tileSetName]

        if (editTileTypeName !in tileSet)
            return returnState

        val tileType = tileSet[editTileTypeName]

        Game.gui.begin()

        drawTitle {
            returnState = it
        }

        drawMaterialEditor(Game.materials.names.first())

        assetSelector.drawAssetSelectionPanel(Game.gui)

        Game.gui.end()

        return returnState
    }

    override fun dispose() {
        assetSelector.dispose()
    }
}