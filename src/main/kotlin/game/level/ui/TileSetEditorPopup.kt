package game.level.ui

import com.cozmicgames.Kore
import com.cozmicgames.files.writeString
import com.cozmicgames.graphics
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.maths.Vector2
import engine.Game
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.GUIPopup
import engine.graphics.ui.layout.absolute
import engine.graphics.ui.widgets.*
import game.assets.types.MaterialAssetType
import game.extensions.deletable
import game.extensions.materialPreview
import game.extensions.plusButton
import game.level.TileSet
import kotlin.math.min

class TileSetEditorPopup : GUIPopup() {
    private val assetSelectorData = AssetSelectorData()
    private val materialEditorData = MaterialEditorData()

    private val materialEditorScroll = Vector2()
    private var currentMaterial: String? = null
    private val tilesScroll = Vector2()
    private var currentTileType: String? = null
    private val tileTypeEditorScroll = Vector2()
    private var currentRuleIndex: Int? = null

    private val tempTileSet = TileSet("temp")
    private var tileSetName: String? = null

    fun reset(tileSetName: String) {
        tempTileSet.clear()
        Game.tileSets[tileSetName]?.let {
            tempTileSet.set(it)
        }
        this.tileSetName = tileSetName
    }

    override fun draw(gui: GUI, width: Float, height: Float): GUIElement {
        fun drawBackground() {
            gui.transient(addToLayer = false) {
                gui.colorRectangle(Color(0.5f, 0.5f, 0.5f, 0.25f), Kore.graphics.width.toFloat(), Kore.graphics.height.toFloat())
            }
        }

        fun drawTitle() {
            val label = {
                gui.label("Edit Tileset", null)
            }

            val cancelButton = {
                gui.textButton("Cancel") {
                    closePopup()
                }
            }

            val saveButton = {
                gui.textButton("Save") {
                    tileSetName?.let {
                        val tileSet = Game.tileSets[it]
                        tileSet?.clear()
                        tileSet?.set(tempTileSet)
                        Game.tileSets.getFileHandle(it)?.writeString(Properties().also { tileSet?.write(it) }.write(), false)
                    }
                    closePopup()
                }
            }

            val labelWidth = gui.getElementSize(label).width

            val buttonsWidth = gui.getElementSize {
                gui.sameLine {
                    cancelButton()
                    saveButton()
                }
            }.width

            gui.group(Game.editorStyle.panelTitleBackgroundColor) {
                gui.sameLine {
                    label()
                    gui.spacing(Kore.graphics.width - labelWidth - buttonsWidth)
                    cancelButton()
                    saveButton()
                }
            }
        }

        fun drawMaterialEditor(materialName: String): Float {
            val materialEditorSize = gui.getElementSize {
                gui.materialEditor(materialName, materialEditorData)
            }

            val panelWidth = materialEditorSize.width + gui.skin.scrollbarSize + gui.skin.elementPadding * 3.0f
            val panelHeight = min(Kore.graphics.height - gui.skin.elementSize - Kore.graphics.height * Game.editorStyle.assetSelectorHeight, materialEditorSize.height)

            gui.setLastElement(gui.absolute(Kore.graphics.width - panelWidth, gui.skin.elementSize))
            gui.panel(panelWidth, panelHeight, materialEditorScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
                gui.label("Material Editor", null)
            }) {
                gui.materialEditor(materialName, materialEditorData)
            }

            return materialEditorSize.width
        }

        fun drawTileList() {
            val titleLabel = {
                gui.label("Tile types")
            }

            val panelWidth = gui.getElementSize(titleLabel).width
            val panelHeight = Kore.graphics.height - gui.skin.elementSize - Kore.graphics.height * Game.editorStyle.assetSelectorHeight

            gui.panel(panelWidth, panelHeight, tilesScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
                titleLabel()
            }) {
                val imageSize = panelWidth - gui.skin.scrollbarSize - gui.skin.elementPadding * 3.0f

                for (name in tempTileSet.tileTypeNames) {
                    if (name !in tempTileSet)
                        continue

                    val tileType = tempTileSet[name] ?: continue

                    val isSelected = currentTileType == name

                    gui.deletable({
                        val texture = Game.textures[Game.materials[tileType.defaultMaterial]?.colorTexturePath ?: "<missing>"]

                        gui.selectableImage(texture, imageSize, imageSize, isSelected) {
                            currentTileType = name
                        }
                    }, imageSize * 0.25f) {
                        tempTileSet.remove(name)
                    }
                }

                gui.plusButton(imageSize) {
                    tempTileSet.addType()
                }
            }
        }

        fun drawTileTypeEditor() {
            if (currentTileType == null)
                return

            val tileType = tempTileSet[requireNotNull(currentTileType)] ?: return

            val x = gui.getElementSize {
                gui.label("Tile types")
            }.width

            val panelWidth = gui.getElementSize { gui.label("Tile types", null) }.width
            val panelHeight = Kore.graphics.height - gui.skin.elementSize - Kore.graphics.height * Game.editorStyle.assetSelectorHeight

            gui.panel(panelWidth, panelHeight, tileTypeEditorScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor) {
                val imageSize = panelWidth - gui.skin.scrollbarSize - gui.skin.elementPadding * 3.0f

                gui.label("Default", Game.editorStyle.panelTitleBackgroundColor, minWidth = panelWidth)

                val defaultMaterial = Game.materials[tileType.defaultMaterial] ?: Game.graphics2d.missingMaterial

                gui.selectable({
                    gui.droppable<MaterialAssetType.MaterialAsset>({
                        tileType.defaultMaterial = it.name
                    }) {
                        gui.materialPreview(defaultMaterial, imageSize)
                    }
                }, currentMaterial == tileType.defaultMaterial) {
                    currentMaterial = tileType.defaultMaterial
                }

                gui.separator(panelWidth)

                gui.label("Rules", Game.editorStyle.panelTitleBackgroundColor, minWidth = panelWidth)

                tileType.rules.forEachIndexed { index, rule ->
                    val isSelected = currentRuleIndex == index

                    val material = Game.materials[rule.material] ?: Game.graphics2d.missingMaterial

                    gui.deletable({
                        gui.selectable({
                            gui.materialPreview(material, imageSize)
                        }, isSelected) {
                            currentRuleIndex = index
                            currentMaterial = rule.material
                        }
                    }, imageSize * 0.25f) {
                        tileType.removeRule(rule)
                    }
                }

                gui.plusButton(imageSize) {
                    tileType.addRule()
                }
            }
        }

        fun drawRuleEditor(width: Float) {
            //TODO
        }

        fun drawAssetSelector() {
            gui.transient {
                val assetSelectorWidth = Kore.graphics.width.toFloat()
                val assetSelectorHeight = Kore.graphics.height * Game.editorStyle.assetSelectorHeight

                gui.setLastElement(gui.absolute(0.0f, Kore.graphics.height - assetSelectorHeight))
                gui.assetSelector(assetSelectorData, assetSelectorWidth, assetSelectorHeight)
            }
        }

        gui.setLastElement(gui.absolute(0.0f, 0.0f))

        return gui.group {
            drawBackground()
            drawTitle()

            val leftColumn = gui.sameLine {
                drawTileList()
                drawTileTypeEditor()
            }

            val rightColumnWidth = currentMaterial?.let {
                drawMaterialEditor(it)
            } ?: 0.0f

            val ruleEditorWidth = Kore.graphics.width - leftColumn.width - rightColumnWidth

            drawRuleEditor(ruleEditorWidth)
            drawAssetSelector()
        }
    }
}