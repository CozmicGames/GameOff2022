package game.assets.types

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.nameWithoutExtension
import engine.Game
import engine.graphics.font.HAlign
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement
import engine.graphics.ui.GUIPopup
import engine.graphics.ui.TextData
import engine.graphics.ui.widgets.*
import game.assets.MetaFile
import game.level.editorStyle

class CreateFilePopup : GUIPopup() {
    private val nameTextData = TextData {}
    private lateinit var onCreate: (String) -> Unit

    fun reset(onCreate: (String) -> Unit) {
        this.onCreate = onCreate
    }

    override fun draw(gui: GUI, width: Float, height: Float): GUIElement {
        return gui.dropShadow(Game.editorStyle.createFilePopupDropShadowColor) {
            gui.bordered(Game.editorStyle.createFilePopupBorderColor, Game.editorStyle.createFilePopupBorderSize) {
                gui.group(Game.editorStyle.createFilePopupContentBackgroundColor) {
                    val cancelButton = {
                        gui.textButton("Cancel") {
                            closePopup()
                        }
                    }

                    val cancelButtonSize = if (width > 0.0f) gui.getElementSize(cancelButton).width else 0.0f

                    val createButton = {
                        gui.textButton("Create") {
                            onCreate(nameTextData.text)

                            val assetFile = Kore.files.local("assets/${nameTextData.text}")
                            val metaFile = MetaFile()
                            metaFile.name = nameTextData.text
                            metaFile.write(assetFile.sibling("${assetFile.nameWithoutExtension}.meta"))
                            closePopup()
                        }
                    }

                    val createButtonSize = if (width > 0.0f) gui.getElementSize(createButton).width else 0.0f

                    gui.label("Create file", Game.editorStyle.createFilePopupTitleBackgroundColor, minWidth = if (width > 0.0f) width else null, align = HAlign.CENTER)

                    gui.textField(nameTextData, width)

                    gui.group(Game.editorStyle.createFilePopupTitleBackgroundColor) {
                        gui.sameLine {
                            cancelButton()
                            gui.spacing(width - cancelButtonSize - createButtonSize)
                            createButton()
                        }
                    }
                }
            }
        }
    }
}