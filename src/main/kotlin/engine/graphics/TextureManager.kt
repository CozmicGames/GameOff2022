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
    inner class Getter(val fileHandle: FileHandle, val name: String, val filter: Texture.Filter) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(fileHandle, name, filter)
    }

    data class TextureKey(val filter: Texture.Filter)

    private val textures = hashMapOf<TextureKey, TextureAtlas>()
    private val keys = hashMapOf<String, TextureKey>()

    val names get() = keys.keys.toList()

    fun add(file: FileHandle, name: String = file.fullPath, filter: Texture.Filter = Texture.Filter.NEAREST) {
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

        add(name, image, filter)
    }

    fun add(name: String, image: Image, filter: Texture.Filter = Texture.Filter.NEAREST) {
        val key = TextureKey(filter)
        val atlas = getAtlas(key)
        atlas.add(name to image)
        keys[name] = key
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
        val key = keys[name] ?: return Game.graphics2d.missingTexture.asRegion()
        val texture = textures[key] ?: return Game.graphics2d.missingTexture.asRegion()
        return texture[name] ?: Game.graphics2d.missingTexture.asRegion()
    }

    fun getOrAdd(fileHandle: FileHandle, name: String = fileHandle.fullPath, filter: Texture.Filter = Texture.Filter.NEAREST): TextureRegion {
        if (fileHandle !in this)
            add(fileHandle, name, filter)

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

    operator fun invoke(fileHandle: FileHandle, name: String = fileHandle.fullPath, filter: Texture.Filter = Texture.Filter.NEAREST) = Getter(fileHandle, name, filter)
}
