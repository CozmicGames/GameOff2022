package game.assets.types

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.FileHandle
import com.cozmicgames.utils.extensions.nameWithExtension
import engine.Game
import engine.graphics.ui.GUI
import engine.graphics.ui.TextData
import engine.graphics.ui.widgets.label
import engine.graphics.ui.widgets.textField
import game.assets.AssetType

abstract class SimpleImportPopup(type: AssetType<*>, titleString: String) : ImportPopup(type, titleString) {
    private lateinit var file: String

    private val nameTextData = TextData {}

    protected abstract fun onImport(file: FileHandle)

    override fun reset(file: String) {
        this.file = file
        nameTextData.setText(file.nameWithExtension)
    }

    override fun drawContent(gui: GUI, width: Float, height: Float) {
        gui.sameLine {
            gui.group {
                gui.label("Original filename", null)
                gui.label("Import filename", null)
            }
            gui.group {
                val originalWidth = gui.label(file, null).width
                gui.textField(nameTextData, originalWidth)
            }
        }
    }

    override fun onImport() {
        val assetFile = Game.assets.getAssetFileHandle(nameTextData.text)

        if (file != nameTextData.text) {
            if (assetFile.exists)
                assetFile.delete()

            Game.assets.importFile(Kore.files.absolute(file), assetFile)
        }

        onImport(assetFile)
    }
}
