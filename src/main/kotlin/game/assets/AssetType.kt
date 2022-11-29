package game.assets

import com.cozmicgames.files.FileHandle
import engine.graphics.ui.DragDropData
import engine.graphics.ui.GUI
import engine.graphics.ui.GUIElement

interface AssetType<T : AssetType<T>> {
    val name: String
    val iconName: String

    val supportedFormats: List<String>

    val assetNames: List<String>

    fun preview(gui: GUI, size: Float, name: String)

    fun createDragDropData(name: String): () -> DragDropData<*>

    fun appendToAssetList(gui: GUI, list: MutableList<() -> GUIElement>)

    fun load(file: FileHandle)
}