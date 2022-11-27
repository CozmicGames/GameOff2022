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
import game.extensions.multilineListWithSameElementWidths
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
    private val imageImportPopup = ImageImportPopup()
    private val filterTextData = TextData {

    }

    fun drawAssetSelectionPanel(gui: GUI = Game.gui, filter: (AssetType) -> Boolean = { true }) {
        val filteredAssetTypes = AssetType.values().filter(filter)

        val panelWidth = Kore.graphics.width * Game.editorStyle.assetSelectionPanelWidth
        val panelHeight = Kore.graphics.height * Game.editorStyle.assetSelectionPanelHeight

        gui.setLastElement(gui.absolute((Kore.graphics.width - panelWidth) * 0.5f, Kore.graphics.height - panelHeight))
        gui.panel(panelWidth, panelHeight, assetPanelScroll, Game.editorStyle.panelContentBackgroundColor, Game.editorStyle.panelTitleBackgroundColor, {
            val assetTypeSelector = {
                gui.sameLine {
                    filteredAssetTypes.forEach {
                        gui.selectableText(it.typeName, Game.textures[it.textureName], currentAssetType == it) {
                            currentAssetType = it
                            assetPanelScroll.setZero()
                        }
                        gui.spacing(gui.skin.elementPadding)
                    }
                }
            }

            val filterText = {
                gui.sameLine {
                    gui.image(Game.textures["assets/images/search.png"], borderThickness = 0.0f)
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
            fun drawAssetList(names: List<String>, createDragDropData: (String) -> () -> DragDropData<*>, getPreviewTexture: (String) -> TextureRegion, fileFilter: Array<out String>, addNewAsset: (String) -> Unit) {
                val elements = names.filter {
                    if (filterTextData.text.isNotBlank())
                        filterTextData.text in it
                    else
                        true
                }.mapTo(arrayListOf()) {
                    {
                        gui.draggable(createDragDropData(it)) {
                            gui.group {
                                gui.image(getPreviewTexture(it), Game.editorStyle.assetElementWidth, borderThickness = 0.0f)
                                gui.tooltip(gui.label(it, backgroundColor = null, maxWidth = Game.editorStyle.assetElementWidth), it)
                            }
                        }
                    }
                }

                elements += {
                    gui.plusButton(Game.editorStyle.assetElementWidth) {
                        Kore.dialogs.open("Open file", filters = fileFilter)?.let {
                            addNewAsset(it)
                        }
                    }
                }

                gui.multilineListWithSameElementWidths(panelWidth - gui.skin.elementPadding - gui.skin.scrollbarSize, Game.editorStyle.assetElementWidth, Game.editorStyle.assetElementMinPadding) {
                    elements.removeFirstOrNull()
                }
            }

            when (currentAssetType) {
                AssetType.MATERIAL -> drawAssetList(Game.materials.names, { { MaterialDragDropData(it) } }, { Game.textures["assets/images/assettype_material.png"] }, arrayOf("*.material"), { Game.materials.add(Kore.files.absolute(it)) })
                AssetType.FONT -> drawAssetList(Game.fonts.names, { { FontDragDropData(it) } }, { Game.textures["assets/images/assettype_font.png"] }, Kore.graphics.supportedFontFormats.toList().toTypedArray(), { Game.fonts.add(Kore.files.absolute(it)) })
                AssetType.SHADER -> drawAssetList(Game.shaders.names, { { ShaderDragDropData(it) } }, { Game.textures["assets/images/assettype_shader.png"] }, arrayOf("*.shader"), { Game.shaders.add(Kore.files.absolute(it)) })
                AssetType.TEXTURE -> drawAssetList(Game.textures.names, { { TextureDragDropData(it) } }, { Game.textures[it] }, Kore.graphics.supportedImageFormats.toList().toTypedArray(), { file ->
                    imageImportPopup.reset(file)
                    gui.popup(imageImportPopup)
                })
                AssetType.SOUND -> drawAssetList(Game.sounds.names, { { SoundDragDropData(it) } }, { Game.textures["assets/images/assettype_sound.png"] }, Kore.audio.supportedSoundFormats.toList().toTypedArray(), { Game.sounds.add(Kore.files.absolute(it)) })
                AssetType.TILESET -> drawAssetList(Game.tileSets.names, { { TilesetDragDropData(it) } }, { Game.textures["assets/images/assettype_tileset.png"] }, arrayOf("*.tileset"), { Game.tileSets.add(Kore.files.absolute(it)) })
            }
        }
    }

    override fun dispose() {
        imageImportPopup.dispose()
    }
}