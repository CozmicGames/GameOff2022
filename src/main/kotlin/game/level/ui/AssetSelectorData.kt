package game.level.ui

import com.cozmicgames.utils.maths.Vector2
import engine.graphics.ui.TextData
import game.assets.AssetType

class AssetSelectorData {
    var showInternalAssetElements = false
    var currentAssetType: String? = null
    var showEditIcons = false
    var filter: (AssetType<*>) -> Boolean = { true }
    val elementsScroll = Vector2()
    val assetTitleScroll = Vector2()
    val filterTextData = TextData { }
}