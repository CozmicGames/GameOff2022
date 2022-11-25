package engine.graphics

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.graphics
import com.cozmicgames.graphics.Image
import com.cozmicgames.graphics.gpu.Texture
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import engine.Game
import kotlin.reflect.KProperty

class TextureManager : Disposable {
    inner class Getter(private val fileHandle: FileHandle, filter: Texture.Filter) {
        init {
            if (fileHandle !in this@TextureManager)
                add(fileHandle, filter)
        }

        operator fun getValue(thisRef: Any, property: KProperty<*>) = get(fileHandle)
    }

    data class TextureKey(val filter: Texture.Filter)

    private val textures = hashMapOf<TextureKey, TextureAtlas>()
    private val keys = hashMapOf<String, TextureKey>()

    val names get() = keys.keys.toList()

    fun add(fileHandle: FileHandle, filter: Texture.Filter = Texture.Filter.NEAREST) {
        val image = if (fileHandle.exists)
            Kore.graphics.readImage(fileHandle)
        else {
            Kore.log.error(this::class, "Texture file not found: $fileHandle")
            return
        }

        if (image == null) {
            Kore.log.error(this::class, "Failed to load texture file: $fileHandle")
            return
        }

        add(fileHandle.fullPath, image, filter)
    }

    fun add(file: String, image: Image, filter: Texture.Filter = Texture.Filter.NEAREST) {
        val key = TextureKey(filter)
        val atlas = getAtlas(key)
        atlas.add(file to image)
        keys[file] = key
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in keys

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(file: String) {
        val key = keys[file] ?: return
        textures[key]?.remove(file)
    }

    operator fun get(fileHandle: FileHandle) = get(fileHandle.fullPath)

    operator fun get(name: String): TextureRegion {
        val returnMissing = {
            Kore.log.error(this::class, "Couldn't find texture '$name', using missing texture instead.")
            Game.graphics2d.missingTexture.asRegion()
        }

        val key = keys[name] ?: return returnMissing()
        val texture = textures[key] ?: return returnMissing()
        return texture[name] ?: returnMissing()
    }

    fun getOrAdd(fileHandle: FileHandle, filter: Texture.Filter = Texture.Filter.NEAREST): TextureRegion {
        if (fileHandle !in this)
            add(fileHandle, filter)

        return this[fileHandle]
    }

    fun getAtlas(key: TextureKey): TextureAtlas {
        return textures.getOrPut(key) {
            when (key.filter) {
                Texture.Filter.NEAREST -> TextureAtlas(sampler = Game.graphics2d.pointClampSampler)
                Texture.Filter.LINEAR -> TextureAtlas(sampler = Game.graphics2d.linearClampSampler)
            }
        }
    }

    override fun dispose() {
        textures.forEach { (_, texture) ->
            texture.dispose()
        }
    }

    operator fun invoke(fileHandle: FileHandle, filter: Texture.Filter = Texture.Filter.NEAREST) = Getter(fileHandle, filter)
}
