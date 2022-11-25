package game.level

import com.cozmicgames.Kore
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.injector
import engine.Game

class EditorStyle {
    var toolImageSize = 40.0f
    val panelContentBackgroundColor = Color(0x575B5BFF)
    val panelTitleBackgroundColor = Color(0x424444FF)
    var assetSelectionPanelWidth = 0.8f
    var assetSelectionPanelHeight = 0.3f
    var assetElementWidth = 50.0f
    var assetElementMinPadding = 6.0f
    var imageImportPreviewSize = 0.3f
    var materialEditorPreviewSize = 0.2f
}

val Game.editorStyle by Kore.context.injector { EditorStyle() }
