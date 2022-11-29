package game.assets.types

import com.cozmicgames.Kore
import com.cozmicgames.dialogs
import com.cozmicgames.files
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.nameWithoutExtension
import com.cozmicgames.graphics
import com.cozmicgames.graphics.Image
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.gpu.Texture2D
import com.cozmicgames.graphics.split
import com.cozmicgames.graphics.toTexture2D
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.extensions.extension
import com.cozmicgames.utils.extensions.nameWithoutExtension
import engine.Game
import engine.graphics.asRegion
import engine.graphics.ui.*
import engine.graphics.ui.widgets.*
import game.assets.AssetType
import game.assets.TextureMetaFile
import game.extensions.downButton
import game.extensions.importButton
import game.extensions.upButton
import game.level.editorStyle
import kotlin.math.max
import kotlin.math.min

class TextureAssetType : AssetType<TextureAssetType>, Disposable {
    inner class ImageImportPopup : ImportPopup(this, "Import texture"), Disposable {
        lateinit var file: String

        private lateinit var image: Image
        private var previewTexture: Texture2D? = null

        private val filterComboboxData = ComboboxData(*Texture.Filter.values())
        private var splitToTiles = false
        private var excludeEmptyImages = true
        private val tileWidthTextColor = Game.gui.skin.fontColor.copy()
        private val tileHeightTextColor = Game.gui.skin.fontColor.copy()

        private val tileWidthTextData = TextData {
            val width = text.toIntOrNull()
            if (width != null) {
                tileWidth = width
                tileWidthTextColor.set(Game.gui.skin.fontColor)
            } else
                tileWidthTextColor.set(Color.RED)
        }

        private val tileHeightTextData = TextData {
            val height = text.toIntOrNull()
            if (height != null) {
                tileHeight = height
                tileHeightTextColor.set(Game.gui.skin.fontColor)
            } else
                tileHeightTextColor.set(Color.RED)
        }

        private val nameTextData = TextData {}

        private var tileWidth: Int
            get() = tileWidthTextData.text.toIntOrNull() ?: 1
            set(value) = tileWidthTextData.setText(max(1, value).toString())

        private var tileHeight: Int
            get() = tileHeightTextData.text.toIntOrNull() ?: 1
            set(value) = tileHeightTextData.setText(max(1, value).toString())

        override fun reset(file: String) {
            this.file = file
            nameTextData.setText(file.nameWithoutExtension)

            previewTexture?.dispose()
            image = requireNotNull(Kore.graphics.readImage(Kore.files.absolute(file)))
            previewTexture = image.toTexture2D(Game.graphics2d.pointClampSampler)

            filterComboboxData.selectedIndex = 0
            splitToTiles = false
            excludeEmptyImages = true
            tileWidth = 1
            tileHeight = 1
        }

        override fun drawContent(gui: GUI, width: Float, height: Float) {
            previewTexture?.let {
                it.setSampler(
                    when (filterComboboxData.selectedItem) {
                        Texture.Filter.LINEAR -> Game.graphics2d.linearClampSampler
                        else -> Game.graphics2d.pointClampSampler
                    }
                )

                val previewImageWidth = Game.editorStyle.imageImportPreviewSize * min(Kore.graphics.width, Kore.graphics.height)
                val previewImageHeight = previewImageWidth * image.height.toFloat() / image.width.toFloat()

                val (linesX, linesY) = gui.getLastElement()

                val previewImageOffset = (width - previewImageWidth) * 0.5f

                gui.offset(previewImageOffset, 0.0f, resetX = true) {
                    gui.image(it.asRegion(), previewImageWidth, previewImageHeight, borderThickness = 0.0f)
                }

                if (splitToTiles) {
                    val linesHorizontal = image.width / tileWidth - 1
                    val linesVertical = image.height / tileHeight - 1

                    val lineSpacingHorizontal = previewImageWidth / (linesHorizontal + 1)
                    val lineSpacingVertical = previewImageHeight / (linesVertical + 1)

                    repeat(linesHorizontal) {
                        val x = previewImageOffset + linesX + (it + 1) * lineSpacingHorizontal
                        gui.currentCommandList.drawLine(x, linesY, x, linesY + previewImageHeight, 2.5f, gui.skin.fontColor)
                    }

                    repeat(linesVertical) {
                        val y = linesY + (it + 1) * lineSpacingVertical
                        gui.currentCommandList.drawLine(previewImageOffset + linesX, y, previewImageOffset + linesX + previewImageWidth, y, 2.5f, gui.skin.fontColor)
                    }
                }
            }

            if (splitToTiles) {
                val columns = image.width / max(tileWidth, 1)
                val rows = image.height / max(tileHeight, 1)

                gui.label("$columns x $rows tiles", null)
            }

            gui.sameLine {
                val labelsWidth = gui.group {
                    gui.label("Filter", null)
                    gui.label("Split to tiles", null)
                    gui.label("Exclude empty images", null)
                    gui.label("Tile width", null)
                    gui.label("Tile height", null)
                    gui.label("Original filename", null)
                    gui.label("Import filename", null)
                }.width

                gui.group {
                    gui.combobox(filterComboboxData)
                    gui.checkBox(splitToTiles) { splitToTiles = it }
                    gui.checkBox(excludeEmptyImages) { excludeEmptyImages = it }
                    gui.sameLine {
                        gui.textField(tileWidthTextData)
                        gui.group {
                            gui.upButton(gui.skin.elementSize * 0.5f) {
                                tileWidth++
                            }
                            gui.downButton(gui.skin.elementSize * 0.5f) {
                                tileWidth--
                            }
                        }
                    }
                    gui.sameLine {
                        gui.textField(tileHeightTextData)
                        gui.group {
                            gui.upButton(Game.gui.skin.elementSize * 0.5f) {
                                tileHeight++
                            }
                            gui.downButton(Game.gui.skin.elementSize * 0.5f) {
                                tileHeight--
                            }
                        }
                    }
                    gui.tooltip(gui.label(file, null, maxWidth = width - labelsWidth), file)
                    gui.textField(nameTextData, labelsWidth)
                }
            }
        }

        override fun onImport() {
            val selectedFilter = filterComboboxData.selectedItem ?: Texture.Filter.NEAREST

            if (splitToTiles) {
                val columns = image.width / max(tileWidth, 1)
                val rows = image.height / max(tileHeight, 1)
                val images = image.split(columns, rows)

                repeat(images.width) { x ->
                    repeat(images.height) { y ->
                        images[x, y]?.let {
                            if (!(excludeEmptyImages && it.pixels.data.all { it.data.all { it == 0.0f } })) {
                                val imageFileName = "${nameTextData.text}_${x}_${y}.${file.extension}"
                                val assetFile = Game.assets.getAssetFileHandle(imageFileName)

                                if (assetFile.exists)
                                    assetFile.delete()

                                Kore.graphics.writeImage(assetFile, it)

                                val metaFile = TextureMetaFile()
                                metaFile.name = imageFileName
                                metaFile.filter = selectedFilter
                                metaFile.write(assetFile.sibling("${assetFile.nameWithoutExtension}.meta"))

                                Game.textures.add(imageFileName, it, selectedFilter)
                            }
                        }
                    }
                }
            } else {
                val assetFile = Game.assets.getAssetFileHandle(nameTextData.text)

                if (file != nameTextData.text) {
                    if (assetFile.exists)
                        assetFile.delete()

                    Game.assets.importFile(Kore.files.absolute(file), assetFile)
                }

                Game.textures.add(assetFile, filter = selectedFilter)
            }
        }

        override fun dispose() {
            previewTexture?.dispose()
        }
    }

    class TextureAsset(val name: String)

    override val name = AssetTypes.TEXTURES

    override val iconName = "internal/images/assettype_texture.png"

    override val supportedFormats get() = Kore.graphics.supportedImageFormats.toList()

    override val assetNames get() = Game.textures.names

    private val imageImportPopup = ImageImportPopup()

    override fun preview(gui: GUI, size: Float, name: String) {
        gui.image(Game.textures[name], size)
    }

    override fun createDragDropData(name: String) = { DragDropData(TextureAsset(name)) { image(Game.textures[name], Game.editorStyle.assetElementWidth) } }

    override fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>) {
        list += {
            gui.importButton(Game.editorStyle.assetElementWidth) {
                Kore.dialogs.open("Open file", filters = Kore.graphics.supportedImageFormats.toList().toTypedArray())?.let {
                    imageImportPopup.reset(it)
                    gui.popup(imageImportPopup)
                }
            }
        }
    }

    override fun load(file: FileHandle) {
        val metaFileHandle = file.sibling("${file.nameWithoutExtension}.meta")

        var filter = Texture.Filter.NEAREST

        val name = if (metaFileHandle.exists) {
            val metaFile = TextureMetaFile()
            metaFile.read(metaFileHandle)
            filter = metaFile.filter
            metaFile.name
        } else
            file.fullPath

        Game.textures.add(file, name, filter)
    }

    override fun dispose() {
        imageImportPopup.dispose()
    }
}