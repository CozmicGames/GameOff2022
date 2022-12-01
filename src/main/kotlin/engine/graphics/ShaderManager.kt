package engine.graphics

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import engine.graphics.shaders.DefaultShader
import engine.graphics.shaders.Shader
import kotlin.reflect.KProperty

class ShaderManager : Disposable {
    inner class Getter(val file: FileHandle, val name: String) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file, name)
    }

    private class Entry(val value: Shader, val file: FileHandle?)

    private val shaders = hashMapOf<String, Entry>()

    val names get() = shaders.keys.toList()

    init {
        add("default", DefaultShader)
    }

    fun add(file: FileHandle, name: String = file.fullPath) {
        if (!file.exists) {
            Kore.log.error(this::class, "Shader file not found: $file")
            return
        }

        val shader = try {
            Shader(file)
        } catch (e: Exception) {
            Kore.log.error(this::class, "Failed to load shader file: $file")
            return
        }

        add(name, shader, file)
    }

    fun add(name: String, shader: Shader, file: FileHandle? = null) {
        shaders[name] = Entry(shader, file)
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in shaders

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(name: String) {
        shaders.remove(name)
    }

    operator fun get(file: FileHandle) = get(file.fullPath)

    operator fun get(name: String): Shader? {
        return shaders[name]?.value
    }

    fun getFileHandle(name: String) = shaders[name]?.file

    fun getOrAdd(file: FileHandle, name: String = file.fullPath): Shader {
        if (name !in this)
            add(file, name)

        return requireNotNull(this[name])
    }

    override fun dispose() {
        shaders.forEach { (_, shader) ->
            (shader.value as? Disposable)?.dispose()
        }
    }

    operator fun invoke(file: FileHandle, name: String = file.fullPath) = Getter(file, name)
}