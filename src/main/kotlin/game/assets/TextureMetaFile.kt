package game.assets

import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.utils.extensions.enumValueOfOrDefault
import com.cozmicgames.utils.string

open class TextureMetaFile : MetaFile() {
    var filterName by string { Texture.Filter.NEAREST.name }

    var filter: Texture.Filter
        set(value) {
            filterName = value.name
        }
        get() {
            return enumValueOfOrDefault(filterName) { Texture.Filter.NEAREST }
        }
}