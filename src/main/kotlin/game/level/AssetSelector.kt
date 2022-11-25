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
import engine.graphics.ui.*
import engine.graphics.ui.widgets.*
import game.extensions.downButton
import game.extensions.plusButton
import game.extensions.upButton
import kotlin.math.max
import kotlin.math.min

class AssetSelector : Disposable {
    class TextureAsset(val name: String)
    class ShaderAsset(val name: String)
    class MaterialAsset(val name: String)
    class FontAsset(val name: String)
    class SoundAsset(val name: String)
    class TilesetAsset(val name: String)

    private class TextureDragDropData(texture: String) : DragDropData<TextureAsset>(TextureAsset(texture), { image(Game.textures[texture], Game.editorStyle.assetElementWidth) })

    private class ShaderDragDropData(shader: String) : DragDropData<ShaderAsset>(ShaderAsset(shader), { label(shader) })

    private class MaterialDragDropData(material: String) : DragDropData<MaterialAsset>(MaterialAsset(material), { label(material) })

    private class FontDragDropData(font: String) : DragDropData<FontAsset>(FontAsset(font), { label(font) })

    private class SoundDragDropData(sound: String) : DragDropData<SoundAsset>(SoundAsset(sound), { label(sound) })

    private class TilesetDragDropData(tileset: String) : DragDropData<TilesetAsset>(TilesetAsset(tileset), { label(tileset) })

    enum class AssetType(val typeName: String, val textureName: String) {
        MATERIAL("Materials", "assets/images/assettype_material.png"),
        TEXTURE("Textures", "assets/images/assettype_texture.png"),
        SHADER("Shaders", "assets/images/assettype_shader.png"),
        FONT("Fonts", "assets/images/assettype_font.png"),
        SOUND("Sounds", "assets/images/assettype_sound.png"),
        TILESET("Tilesets", "assets/images/assettype_tileset.png")
    }

    private var currentAssetType = AssetType.values().first()
    private val assetPanelScroll = Vector2()

    private val imageImportPopupSettings = object {
        val data = object {
            lateinit var image: Image
            var previewTexture: Texture2D? = null
        }

        val filterComboboxData = ComboboxData(*Texture.Filter.values())
        var splitToTiles = false
        val tileWidthTextColor = Game.gui.skin.fontColor.copy()
        val tileHeightTextColor = Game.gui.skin.fontColor.copy()

        val tileWidthTextData = TextData {
            val width = text.toIntOrNull()
            if (width != null) {
                tileWidth = width
                tileWidthTextColor.set(Game.gui.skin.fontColor)
            } else
                tileWidthTextColor.set(Color.RED)
        }

        val tileHeightTextData = TextData {
            val height = text.toIntOrNull()
            if (height != null) {
                tileHeight = height
                tileHeightTextColor.set(Game.gui.skin.fontColor)
            } else
                tileHeightTextColor.set(Color.RED)
        }

        var tileWidth: Int
            get() = tileWidthTextData.text.toIntOrNull() ?: 1
            set(value) = tileWidthTextData.setText(max(1, value).toString())

        var tileHeight: Int
            get() = tileHeightTextData.text.toIntOrNull() ?: 1
            set(value) = tileHeightTextData.setText(max(1, value).toString())

        fun reset(file: String) {
            data.previewTexture?.dispose()
            data.image = requireNotNull(Kore.graphics.readImage(Kore.files.absolute(file)))
            data.previewTexture = data.image.toTexture2D(Game.graphics2d.pointClampSampler)

            filterComboboxData.selectedIndex = 0
            splitToTiles = false
            tileWidth = 1
            tileHeight = 1
        }
    }

    fun drawAssetSelectionPanel(gui: GUI = Game.gui, filter: (AssetType) -> Boolean = { true }) {
        val filteredAssetTypes = AssetType.values().filter(filter)

        val panelWidth = Kore.graphics.width * Game.editorStyle.assetSelectionPanelWidth
        val panelHeight = Kore.graphics.height * Game.editorStyle.assetSelectionPanelHeight

        val assetElementsPerLine = (panelWidth - gui.skin.scrollbarSize).toInt() / (Game.editorStyle.assetElementWidth + Game.editorStyle.assetElementMinPadding).toInt() - 1
        val assetElementPadding = (panelWidth - (assetElementsPerLine + 1) * Game.editorStyle.assetElementWidth) / assetElementsPerLine

        gui.setLastElement(gui.absolute((Kore.graphics.width - panelWidth) * 0.5f, Kore.graphics.height - panelHeight))
        gui.panel(panelWidth, panelHeight, assetPanelScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
            gui.sameLine {
                filteredAssetTypes.forEach {
                    gui.selectableText(it.typeName, Game.textures[it.textureName], currentAssetType == it) {
                        currentAssetType = it
                        assetPanelScroll.setZero()
                    }
                    gui.spacing(gui.skin.elementPadding)
                }
            }
        }) {
            fun drawAssetList(names: List<String>, createDragDropData: (String) -> () -> DragDropData<*>, getPreviewTexture: (String) -> TextureRegion, fileFilter: Array<out String>, addNewAsset: (String) -> Unit) {
                var needsAddButton = true

                val lines = names.chunked(assetElementsPerLine)

                lines.forEachIndexed { lineIndex, lineNames ->
                    gui.sameLine {
                        gui.spacing(assetElementPadding * 0.5f)

                        var index = 0
                        while (index < assetElementsPerLine) {
                            val name = lineNames[index]

                            gui.draggable(createDragDropData(name)) {
                                gui.group {
                                    gui.image(getPreviewTexture(name), Game.editorStyle.assetElementWidth, borderThickness = 0.0f)
                                    gui.tooltip(gui.label(name, backgroundColor = null, maxWidth = Game.editorStyle.assetElementWidth), name)
                                }
                            }

                            if (index < lineNames.lastIndex)
                                gui.spacing(assetElementPadding)

                            if (index == lineNames.lastIndex && index < assetElementsPerLine - 1 && lineIndex == lines.lastIndex) {
                                gui.plusButton(Game.editorStyle.assetElementWidth) {
                                    Kore.dialogs.open("Open file", filters = fileFilter)?.let {
                                        addNewAsset(it)
                                    }
                                }
                                needsAddButton = false
                                break
                            }

                            index++
                        }
                    }

                    if (lineIndex != lines.lastIndex)
                        gui.blankLine(Game.editorStyle.assetElementMinPadding)
                }

                if (needsAddButton)
                    gui.plusButton(Game.editorStyle.assetElementWidth) {
                        Kore.dialogs.open("Open file", filters = fileFilter)?.let {
                            addNewAsset(it)
                        }
                    }
            }

            when (currentAssetType) {
                AssetType.MATERIAL -> drawAssetList(Game.materials.names, { { MaterialDragDropData(it) } }, { Game.textures["assets/images/assettype_material.png"] }, arrayOf("*.material"), { Game.materials.add(Kore.files.absolute(it)) })
                AssetType.FONT -> drawAssetList(Game.fonts.names, { { FontDragDropData(it) } }, { Game.textures["assets/images/assettype_font.png"] }, Kore.graphics.supportedFontFormats.toList().toTypedArray(), { Game.fonts.add(Kore.files.absolute(it)) })
                AssetType.SHADER -> drawAssetList(Game.shaders.names, { { ShaderDragDropData(it) } }, { Game.textures["assets/images/assettype_shader.png"] }, arrayOf("*.shader"), { Game.shaders.add(Kore.files.absolute(it)) })
                AssetType.TEXTURE -> drawAssetList(Game.textures.names, { { TextureDragDropData(it) } }, { Game.textures[it] }, Kore.graphics.supportedImageFormats.toList().toTypedArray(), { file ->
                    imageImportPopupSettings.reset(file)

                    gui.popup { gui, width, height ->
                        gui.group(Game.editorStyle.panelContentBackgroundColor) {
                            val cancelButton = {
                                gui.textButton("Cancel") {
                                    closePopup()
                                }
                            }

                            val cancelButtonSize = if (width > 0.0f) gui.getElementSize(cancelButton).width else 0.0f

                            val importButton = {
                                gui.textButton("Import") {
                                    val selectedFilter = imageImportPopupSettings.filterComboboxData.selectedItem ?: Texture.Filter.NEAREST

                                    if (imageImportPopupSettings.splitToTiles) {
                                        val columns = imageImportPopupSettings.data.image.width / max(imageImportPopupSettings.tileWidth, 1)
                                        val rows = imageImportPopupSettings.data.image.height / max(imageImportPopupSettings.tileHeight, 1)
                                        val images = imageImportPopupSettings.data.image.split(columns, rows)

                                        repeat(images.width) { x ->
                                            repeat(images.height) { y ->
                                                val image = requireNotNull(images[x, y])
                                                Game.textures.add("${file.pathWithoutExtension}_${x}_${y}.${file.extension}", image, selectedFilter)
                                            }
                                        }
                                    } else
                                        Game.textures.add(Kore.files.absolute(file), selectedFilter)

                                    closePopup()
                                }
                            }

                            val importButtonSize = if (width > 0.0f) gui.getElementSize(importButton).width else 0.0f

                            gui.label("Import texture", Game.editorStyle.panelTitleBackgroundColor, minWidth = if (width > 0.0f) width else null, align = HAlign.CENTER)

                            imageImportPopupSettings.data.previewTexture?.let {
                                it.setSampler(
                                    when (imageImportPopupSettings.filterComboboxData.selectedItem) {
                                        Texture.Filter.LINEAR -> Game.graphics2d.linearClampSampler
                                        else -> Game.graphics2d.pointClampSampler
                                    }
                                )

                                val previewImageWidth = Game.editorStyle.imageImportPreviewSize * min(Kore.graphics.width, Kore.graphics.height)
                                val previewImageHeight = previewImageWidth * imageImportPopupSettings.data.image.height.toFloat() / imageImportPopupSettings.data.image.width.toFloat()

                                val (linesX, linesY) = gui.getLastElement()

                                gui.image(it.asRegion(), previewImageWidth, previewImageHeight, borderThickness = 0.0f)

                                if (imageImportPopupSettings.splitToTiles) {
                                    val linesHorizontal = imageImportPopupSettings.data.image.width / imageImportPopupSettings.tileWidth - 1
                                    val linesVertical = imageImportPopupSettings.data.image.height / imageImportPopupSettings.tileHeight - 1

                                    val lineSpacingHorizontal = previewImageWidth / (linesHorizontal + 1)
                                    val lineSpacingVertical = previewImageHeight / (linesVertical + 1)

                                    repeat(linesHorizontal) {
                                        val x = linesX + (it + 1) * lineSpacingHorizontal
                                        gui.currentCommandList.drawLine(x, linesY, x, linesY + previewImageHeight, 2.5f, gui.skin.fontColor)
                                    }

                                    repeat(linesVertical) {
                                        val y = linesY + (it + 1) * lineSpacingVertical
                                        gui.currentCommandList.drawLine(linesX, y, linesX + previewImageWidth, y, 2.5f, gui.skin.fontColor)
                                    }
                                }
                            }

                            gui.sameLine {
                                gui.group {
                                    gui.label("Filter", null)
                                    gui.label("Split to tiles", null)
                                    gui.label("Tile width", null)
                                    gui.label("Tile height", null)
                                }
                                gui.group {
                                    gui.combobox(imageImportPopupSettings.filterComboboxData)
                                    gui.checkBox(imageImportPopupSettings.splitToTiles) { imageImportPopupSettings.splitToTiles = it }
                                    gui.sameLine {
                                        gui.textField(imageImportPopupSettings.tileWidthTextData)
                                        gui.group {
                                            gui.upButton(gui.skin.elementSize * 0.5f) {
                                                imageImportPopupSettings.tileWidth++
                                            }
                                            gui.downButton(gui.skin.elementSize * 0.5f) {
                                                imageImportPopupSettings.tileWidth--
                                            }
                                        }
                                    }
                                    gui.sameLine {
                                        gui.textField(imageImportPopupSettings.tileHeightTextData)
                                        gui.group {
                                            gui.upButton(Game.gui.skin.elementSize * 0.5f) {
                                                imageImportPopupSettings.tileHeight++
                                            }
                                            gui.downButton(Game.gui.skin.elementSize * 0.5f) {
                                                imageImportPopupSettings.tileHeight--
                                            }
                                        }
                                    }
                                }
                            }

                            gui.group(Game.editorStyle.panelTitleBackgroundColor) {
                                gui.sameLine {
                                    cancelButton()
                                    gui.spacing(width - cancelButtonSize - importButtonSize)
                                    importButton()
                                }
                            }
                        }
                    }
                })
                AssetType.SOUND -> drawAssetList(Game.sounds.names, { { SoundDragDropData(it) } }, { Game.textures["assets/images/assettype_sound.png"] }, Kore.audio.supportedSoundFormats.toList().toTypedArray(), { Game.sounds.add(Kore.files.absolute(it)) })
                AssetType.TILESET -> drawAssetList(Game.tileSets.names, { { TilesetDragDropData(it) } }, { Game.textures["assets/images/assettype_tileset.png"] }, arrayOf("*.tileset"), { Game.tileSets.add(Kore.files.absolute(it)) })
            }
        }
    }

    override fun dispose() {
        imageImportPopupSettings.data.previewTexture?.dispose()
    }
}