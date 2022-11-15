package game.states.editor

import com.cozmicgames.utils.collections.Array2D
import com.cozmicgames.utils.collections.FixedSizeStack
import game.components.GridComponent

class LevelEditor(maxCommands: Int = 100) {
    interface Command {
        val isUndoable get() = false

        fun execute(): Boolean
        fun undo()
    }

    private val undoCommands = FixedSizeStack<Command>(maxCommands)
    private val redoCommands = FixedSizeStack<Command>(maxCommands)

    val hasUndoableCommand get() = !undoCommands.isEmpty
    val hasRedoableCommand get() = !redoCommands.isEmpty

    fun execute(command: Command) {
        if (command.isUndoable)
            undoCommands.push(command)

        if (command.execute())
            redoCommands.clear()
    }

    fun undo(): Boolean {
        if (!hasUndoableCommand)
            return false

        val command = undoCommands.pop()
        redoCommands.push(command)
        command.undo()
        return true
    }

    fun redo(): Boolean {
        if (!hasRedoableCommand)
            return false

        val command = redoCommands.pop()
        undoCommands.push(command)
        command.execute()
        return true
    }

    fun clear() {
        undoCommands.clear()
        redoCommands.clear()
    }
}

fun LevelEditor.setTiles(grid: GridComponent, x: Int, y: Int, width: Int, height: Int, tiles: Array2D<String?>) = execute(SetTilesCommand(grid.getRegion(x, y, width, height), tiles))

fun LevelEditor.setTiles(region: GridRegion, tiles: Array2D<String?>) = execute(SetTilesCommand(region, tiles))

fun LevelEditor.setTiles(region: GridRegion, tile: String?) = execute(SetTilesCommand(region, Array2D(region.width, region.height) { _, _ -> tile }))

fun LevelEditor.setTile(grid: GridComponent, x: Int, y: Int, type: String?) = execute(SetTilesCommand(grid.getRegion(x, y, 1, 1), Array2D(1, 1) { _, _ -> type }))
