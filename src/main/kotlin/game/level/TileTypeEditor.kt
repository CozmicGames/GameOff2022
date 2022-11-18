package game.level

import com.cozmicgames.Kore
import com.cozmicgames.graphics
import engine.Game
import engine.graphics.ui.StringDragDropData
import engine.graphics.ui.widgets.*

class TileTypeEditor {
    sealed interface ReturnState {
        object None : ReturnState

        object Menu : ReturnState

        object LevelEditor : ReturnState
    }

    var t = "Test"

    fun onFrame(editTileTypeName: String, tileSetName: String): ReturnState {
        var returnState: ReturnState = ReturnState.None

        if (tileSetName !in Game.tileSets)
            return returnState

        val tileSet = Game.tileSets[tileSetName]

        if (editTileTypeName !in tileSet)
            return returnState

        val tileType = tileSet[editTileTypeName]

        Game.gui.begin()

        Game.gui.setLastElement(Game.gui.absolute(Kore.graphics.width * 0.2f, Kore.graphics.height * 0.2f))
        Game.gui.group(Game.editorStyle.panelBackgroundColor) {
            Game.gui.label("Default material")
            Game.gui.separator()

            Game.gui.label("Rules")
            Game.gui.separator()

            Game.gui.draggable({ StringDragDropData("Hello") }, 2.0f) {
                Game.gui.label("Hello")
            }

            Game.gui.separator()
            Game.gui.separator()

            Game.gui.droppable<String>({ t = it }, 2.0f) {
                Game.gui.label(t)
            }
        }

        Game.gui.setLastElement(Game.gui.absolute(Kore.graphics.width * 0.2f, Kore.graphics.height * 0.7f))
        Game.gui.group(Game.editorStyle.panelBackgroundColor) {
            Game.gui.sameLine {
                val backButton = {
                    Game.gui.textButton("Back") {
                        returnState = ReturnState.LevelEditor
                    }
                }

                val saveButton = {
                    Game.gui.textButton("Save") {
                        returnState = ReturnState.LevelEditor
                    }
                }

                val backButtonWidth = Game.gui.getElementSize(backButton).width
                val saveButtonWidth = Game.gui.getElementSize(saveButton).width

                backButton()

                Game.gui.spacing(Kore.graphics.width * 0.6f - backButtonWidth - saveButtonWidth)

                saveButton()
            }
        }
        Game.gui.end()

        return returnState
    }
}