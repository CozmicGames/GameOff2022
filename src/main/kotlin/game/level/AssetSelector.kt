package game.level

import com.cozmicgames.*
import com.cozmicgames.utils.maths.Vector2
import engine.Game
import engine.graphics.ui.*
import engine.graphics.ui.widgets.*
import game.assets.AssetType
import game.assets.findAssetType
import game.extensions.multilineListWithSameElementWidths

class AssetSelector {
    var showInternalAssetElements = false

    private var currentAssetType: String? = null
    private val assetPanelScroll = Vector2()
    private val filterTextData = TextData {

    }

    fun drawAssetSelectionPanel(gui: GUI = Game.gui, filter: (AssetType<*>) -> Boolean = { true }) {
        val filteredAssetTypes = Game.assets.assetTypes.filter(filter)

        val panelWidth = Kore.graphics.width * Game.editorStyle.assetSelectionPanelWidth
        val panelHeight = Kore.graphics.height * Game.editorStyle.assetSelectionPanelHeight

        gui.setLastElement(gui.absolute((Kore.graphics.width - panelWidth) * 0.5f, Kore.graphics.height - panelHeight))
        gui.panel(panelWidth, panelHeight, assetPanelScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
            val assetTypeSelector = {
                gui.sameLine {
                    filteredAssetTypes.forEach {
                        gui.selectableText(it.name, Game.textures[it.iconName], currentAssetType == it.name) {
                            currentAssetType = it.name
                            assetPanelScroll.setZero()
                        }
                        gui.spacing(gui.skin.elementPadding)
                    }
                }
            }

            val filterText = {
                gui.sameLine {
                    gui.image(Game.textures["internal/images/search.png"], borderThickness = 0.0f)
                    gui.textField(filterTextData, gui.skin.elementSize * 6.0f)
                }
            }

            val assetTypeSelectorWidth = gui.getElementSize(assetTypeSelector).width
            val filterTextWidth = gui.getElementSize(filterText).width

            gui.sameLine {
                assetTypeSelector()
                gui.spacing(panelWidth - assetTypeSelectorWidth - filterTextWidth)
                filterText()
            }
        }) {
            val currentAssetType = this.currentAssetType

            if (currentAssetType != null) {
                val assetType = Game.assets.findAssetType(currentAssetType)

                if (assetType != null) {
                    val elements = assetType.assetNames.filter {
                        if (showInternalAssetElements || !it.startsWith("internal")) {
                            if (filterTextData.text.isNotBlank())
                                filterTextData.text in it
                            else
                                true
                        } else
                            false
                    }.mapTo(arrayListOf()) {
                        {
                            gui.draggable(assetType.createDragDropData(it)) {
                                gui.group {
                                    assetType.preview(gui, Game.editorStyle.assetElementWidth, it)
                                    gui.tooltip(gui.label(it, backgroundColor = null, maxWidth = Game.editorStyle.assetElementWidth), it)
                                }
                            }
                        }
                    }

                    assetType.appendToAssetList(gui, elements)

                    gui.multilineListWithSameElementWidths(panelWidth - gui.skin.elementPadding - gui.skin.scrollbarSize, Game.editorStyle.assetElementWidth, Game.editorStyle.assetElementMinPadding) {
                        elements.removeFirstOrNull()
                    }
                }
            }
        }
    }
}