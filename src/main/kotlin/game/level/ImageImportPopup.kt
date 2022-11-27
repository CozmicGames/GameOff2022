package game.level

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.graphics
import com.cozmicgames.graphics.Image
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.graphics.gpu.Texture2D
import com.cozmicgames.graphics.split
import com.cozmicgames.graphics.toTexture2D
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.extensions.extension
import com.cozmicgames.utils.extensions.pathWithoutExtension
import engine.Game
import engine.graphics.asRegion
import engine.graphics.font.HAlign
import engine.graphics.ui.*
import engine.graphics.ui.widgets.*
import game.extensions.downButton
import game.extensions.upButton
import kotlin.math.max
import kotlin.math.min

class ImageImportPopup : GUIPopup(), Disposable {
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

    private var tileWidth: Int
        get() = tileWidthTextData.text.toIntOrNull() ?: 1
        set(value) = tileWidthTextData.setText(max(1, value).toString())

    private var tileHeight: Int
        get() = tileHeightTextData.text.toIntOrNull() ?: 1
        set(value) = tileHeightTextData.setText(max(1, value).toString())

    fun reset(file: String) {
        this.file = file

        previewTexture?.dispose()
        image = requireNotNull(Kore.graphics.readImage(Kore.files.absolute(file)))
        previewTexture = image.toTexture2D(Game.graphics2d.pointClampSampler)

        filterComboboxData.selectedIndex = 0
        splitToTiles = false
        excludeEmptyImages = true
        tileWidth = 1
        tileHeight = 1
    }

    override fun draw(gui: GUI, width: Float, height: Float): GUIElement {
        return gui.dropShadow(Game.editorStyle.imageImportDropShadowColor) {
            gui.bordered(Game.editorStyle.imageImportPopupBorderColor, 2.5f) {
                gui.group(Game.editorStyle.panelContentBackgroundColor) {
                    val cancelButton = {
                        gui.textButton("Cancel") {
                            closePopup()
                        }
                    }

                    val cancelButtonSize = if (width > 0.0f) gui.getElementSize(cancelButton).width else 0.0f

                    val importButton = {
                        gui.textButton("Import") {
                            val selectedFilter = filterComboboxData.selectedItem ?: Texture.Filter.NEAREST

                            if (splitToTiles) {
                                val columns = image.width / max(tileWidth, 1)
                                val rows = image.height / max(tileHeight, 1)
                                val images = image.split(columns, rows)

                                repeat(images.width) { x ->
                                    repeat(images.height) { y ->
                                        images[x, y]?.let {
                                            if (!(excludeEmptyImages && it.pixels.data.all { it.data.all { it == 0.0f } }))
                                                Game.textures.add("${file.pathWithoutExtension}_${x}_${y}.${file.extension}", it, selectedFilter)
                                        }
                                    }
                                }
                            } else
                                Game.textures.add(Kore.files.absolute(file), selectedFilter)

                            closePopup()
                        }
                    }

                    val importButtonSize = if (width > 0.0f) gui.getElementSize(importButton).width else 0.0f

                    gui.label("Import texture", Game.editorStyle.panelTitleBackgroundColor, minWidth = if (width > 0.0f) width else null, align = HAlign.CENTER)

                    previewTexture?.let {
                        it.setSampler(
                            when (filterComboboxData.selectedItem) {
                                Texture.Filter.LINEAR -> Game.graphics2d.linearClampSampler
                                else -> Game.graphics2d.pointClampSampler
                            }
                        )

                        val previewImageWidth = max(width, Game.editorStyle.imageImportPreviewSize * min(Kore.graphics.width, Kore.graphics.height))
                        val previewImageHeight = previewImageWidth * image.height.toFloat() / image.width.toFloat()

                        val (linesX, linesY) = gui.getLastElement()

                        gui.image(it.asRegion(), previewImageWidth, previewImageHeight, borderThickness = 0.0f)

                        if (splitToTiles) {
                            val linesHorizontal = image.width / tileWidth - 1
                            val linesVertical = image.height / tileHeight - 1

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

                            val columns = image.width / max(tileWidth, 1)
                            val rows = image.height / max(tileHeight, 1)

                            gui.label("$columns x $rows tiles", null)
                        }
                    }

                    gui.sameLine {
                        gui.group {
                            gui.label("Filter", null)
                            gui.label("Split to tiles", null)
                            gui.label("Exclude empty images", null)
                            gui.label("Tile width", null)
                            gui.label("Tile height", null)
                        }
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
        }
    }

    override fun dispose() {
        previewTexture?.dispose()
    }
}