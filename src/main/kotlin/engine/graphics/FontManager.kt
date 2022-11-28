package engine.graphics

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.files.FileHandle
import com.cozmicgames.graphics
import com.cozmicgames.graphics.Font
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import engine.graphics.shaders.Shader
import kotlin.reflect.KProperty

class FontManager : Disposable {
    inner class Getter(val file: FileHandle, val name: String) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file, name)
    }

    private val fonts = hashMapOf<String, Font>()

    val names get() = fonts.keys.toList()

    fun add(file: FileHandle, name: String = file.fullPath) {
        if (!file.exists) {
            Kore.log.error(this::class, "Font file not found: $file")
            return
        }

        val font = Kore.graphics.readFont(file)

        if (font == null) {
            Kore.log.error(this::class, "Failed to load font file: $file")
            return
        }

        add(name, font)
    }

    fun add(name: String, font: Font) {
        fonts[name] = font
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in fonts

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(name: String) {
        fonts.remove(name)
    }

    operator fun get(file: FileHandle) = get(file.fullPath)

    operator fun get(name: String): Font? {
        return fonts[name]
    }

    fun getOrAdd(file: FileHandle, name: String = file.fullPath): Font {
        if (name !in this)
            add(file, name)

        return requireNotNull(this[name])
    }

    override fun dispose() {
        fonts.forEach { (_, font) ->
            (font as? Disposable)?.dispose()
        }
    }

    operator fun invoke(file: FileHandle, name: String = file.fullPath) = Getter(file, name)
}