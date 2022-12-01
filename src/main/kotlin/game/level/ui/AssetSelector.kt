package game.level.ui

import engine.Game
import engine.graphics.ui.*
import engine.graphics.ui.widgets.*
import game.assets.findAssetType
import game.extensions.multilineListWithSameElementWidths

fun GUI.assetSelector(data: AssetSelectorData, width: Float, height: Float): GUIElement {
    val filteredAssetTypes = Game.assets.assetTypes.filter(data.filter)

    return panel(width, height, data.elementsScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
        val assetTypeSelector = {
            sameLine {
                if (filteredAssetTypes.size == 1) {
                    val type = filteredAssetTypes.first()
                    imageLabel(type.name, Game.textures[type.iconName])
                    data.currentAssetType = type.name
                } else
                    filteredAssetTypes.forEach {
                        selectableText(it.name, Game.textures[it.iconName], data.currentAssetType == it.name) {
                            data.currentAssetType = it.name
                            data.elementsScroll.setZero()
                        }
                        spacing(skin.elementPadding)
                    }
            }
        }

        val filterText = {
            sameLine {
                image(Game.textures["internal/images/search.png"], borderThickness = 0.0f)
                textField(data.filterTextData, skin.elementSize * 6.0f)
            }
        }

        val assetTypeSelectorWidth = getElementSize(assetTypeSelector).width
        val filterTextWidth = getElementSize(filterText).width

        scrollArea(maxWidth = width, scroll = data.assetTitleScroll) {
            sameLine {
                assetTypeSelector()
                val spacingAmount = width - assetTypeSelectorWidth - filterTextWidth - skin.elementPadding * 2.0f
                if (spacingAmount > 0.0f)
                    spacing(spacingAmount)
                filterText()
            }
        }
    }) {
        val currentAssetType = data.currentAssetType

        if (currentAssetType != null) {
            val assetType = Game.assets.findAssetType(currentAssetType)

            if (assetType != null) {
                val elements = assetType.assetNames.filter {
                    if (data.showInternalAssetElements || !it.startsWith("internal")) {
                        if (data.filterTextData.text.isNotBlank())
                            data.filterTextData.text in it
                        else
                            true
                    } else
                        false
                }.mapTo(arrayListOf()) {
                    {
                        draggable(assetType.createDragDropData(it)) {
                            group {
                                assetType.preview(this, Game.editorStyle.assetElementWidth, it, data.showEditIcons)
                                tooltip(label(it, backgroundColor = null, maxWidth = Game.editorStyle.assetElementWidth), it)
                            }
                        }
                    }
                }

                assetType.appendToAssetList(this, elements)

                multilineListWithSameElementWidths(width - skin.scrollbarSize - skin.elementPadding * 3.0f, Game.editorStyle.assetElementWidth, Game.editorStyle.assetElementMinPadding) {
                    elements.removeFirstOrNull()
                }
            }
        }
    }
}
