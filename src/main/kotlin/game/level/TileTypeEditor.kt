package game.level

import com.cozmicgames.*
import com.cozmicgames.utils.maths.Vector2
import engine.Game
import engine.graphics.ui.widgets.*
import engine.materials.Material
import game.extensions.materialPreview
import game.extensions.minusButton
import game.extensions.plusButton

class TileTypeEditor {
    companion object {
        private val EMPTY_MATERIAL = Material().also {
            it.colorTexturePath = "internal/images/empty_tiletype.png"
        }
    }

    sealed interface ReturnState {
        object None : ReturnState

        object Menu : ReturnState

        object LevelEditor : ReturnState
    }

    private val assetSelector = AssetSelector()
    private val materialEditor = MaterialEditor()
    private val materialEditorScroll = Vector2()
    private var currentMaterial: String? = null
    private val ruleListScroll = Vector2()
    private var currentRuleIndex: Int? = null

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
        materialEditor.onFrame(materialName)

        val panelWidth = materialEditor.width + Game.gui.skin.scrollbarSize + Game.gui.skin.elementPadding * 3.0f

        Game.gui.setLastElement(Game.gui.absolute(Kore.graphics.width - panelWidth, Game.gui.skin.elementSize))

        Game.gui.panel(panelWidth, Kore.graphics.height - Game.gui.skin.elementSize - Kore.graphics.height * Game.editorStyle.assetSelectionPanelHeight, materialEditorScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
            Game.gui.label("Material Editor", null)
        }) {
            materialEditor.onFrame(materialName)
        }
    }

    fun drawRuleList(tileType: TileSet.TileType) {
        var width = 0.0f

        val drawList = {
            Game.gui.group {
                Game.gui.label("Default", minWidth = width, backgroundColor = Game.editorStyle.panelTitleBackgroundColor)

                Game.gui.selectable({
                    Game.gui.materialPreview(Game.materials[tileType.defaultMaterial] ?: EMPTY_MATERIAL, Game.editorStyle.toolImageSize)
                }, currentRuleIndex == -1) {
                    currentRuleIndex = -1
                    currentMaterial = tileType.defaultMaterial
                }

                Game.gui.separator(width)

                tileType.rules.forEachIndexed { index, rule ->
                    Game.gui.selectable({
                        Game.gui.materialPreview(Game.materials[rule.material] ?: EMPTY_MATERIAL, Game.editorStyle.toolImageSize)
                    }, currentRuleIndex == index) {
                        currentRuleIndex = index
                        currentMaterial = rule.material
                    }
                }

                Game.gui.plusButton(Game.editorStyle.toolImageSize) {
                    tileType.addRule()
                }

                currentRuleIndex?.let {
                    Game.gui.minusButton(Game.editorStyle.toolImageSize) {
                        val rule = tileType.rules.getOrNull(it)
                        rule?.let {
                            tileType.remove(it)
                            currentMaterial = null
                        }
                    }
                }
            }
        }

        width = Game.gui.getElementSize(drawList).width

        val panelWidth = width + Game.gui.skin.scrollbarSize + Game.gui.skin.elementPadding * 3.0f

        Game.gui.setLastElement(Game.gui.absolute(0.0f, Game.gui.skin.elementSize))

        Game.gui.panel(panelWidth, Kore.graphics.height - Game.gui.skin.elementSize - Kore.graphics.height * Game.editorStyle.assetSelectionPanelHeight, ruleListScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
            Game.gui.label("Autotile Rules", null)
        }) {
            drawList()
        }
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

        drawRuleList(tileType)

        currentMaterial?.let {
            drawMaterialEditor(it)
        }

        assetSelector.drawAssetSelectionPanel(Game.gui)

        Game.gui.end()

        return returnState
    }
}