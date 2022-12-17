package game.assets.managers

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.nameWithExtension
import com.cozmicgames.graphics
import com.cozmicgames.graphics.Image
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.log
import engine.Game
import engine.graphics.TextureAtlas
import engine.graphics.TextureRegion
import game.assets.AssetTypeManager

class TextureManager : AssetTypeManager<TextureRegion, Texture.Filter>() {
    override val defaultParams = Texture.Filter.NEAREST

    class Entry(val filter: Texture.Filter, val file: FileHandle?)

    private val textures = hashMapOf<Texture.Filter, TextureAtlas>()
    private val entries = hashMapOf<String, Entry>()

    val names get() = entries.keys.toList()

    override fun add(file: FileHandle, name: String, params: Texture.Filter) {
        val image = if (file.exists)
            Kore.graphics.readImage(file)
        else {
            Kore.log.error(this::class, "Texture file not found: $file")
            return
        }

        if (image == null) {
            Kore.log.error(this::class, "Failed to load texture file: $file")
            return
        }

        add(name, image, params, file)
    }

    fun add(name: String, image: Image, filter: Texture.Filter, file: FileHandle? = null) {
        val atlas = getAtlas(filter)
        atlas.add(name to image)
        entries[name] = Entry(filter, file)
    }

    fun getAtlas(filter: Texture.Filter): TextureAtlas {
        return textures.getOrPut(filter) {
            when (filter) {
                Texture.Filter.NEAREST -> TextureAtlas(sampler = Game.graphics2d.pointClampSampler)
                Texture.Filter.LINEAR -> TextureAtlas(sampler = Game.graphics2d.linearClampSampler)
            }
        }
    }

    override fun contains(name: String) = name in entries

    override fun get(name: String): TextureRegion? {
        val entry = entries[name] ?: return null
        val atlas = getAtlas(entry.filter)
        return atlas[name]
    }

    override fun remove(name: String) {
        val entry = entries.remove(name) ?: return

        val atlas = getAtlas(entry.filter)
        atlas.remove(name)

        val file = entry.file

        if (file != null) {
            if (file.exists && file.isWritable)
                file.delete()

            val metaFile = file.sibling("${file.nameWithExtension}.meta")
            if (metaFile.exists && metaFile.isWritable)
                metaFile.delete()
        }
    }

    override fun getFileHandle(name: String): FileHandle? {
        val entry = entries[name] ?: return null
        return entry.file
    }

    fun getFilter(name: String): Texture.Filter? {
        val entry = entries[name] ?: return null
        return entry.filter
    }

    override fun dispose() {
        textures.forEach { _, texture ->
            texture.dispose()
        }
    }
}