package game.level

import com.cozmicgames.Kore
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.injector
import engine.Game

class EditorStyle {
    var toolImageSize = 40.0f
    val panelBackgroundColor = Color(0x575B5BFF)
}

val Game.editorStyle by Kore.context.injector { EditorStyle() }
