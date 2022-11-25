package engine.graphics

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import engine.graphics.shaders.DefaultShader
import engine.graphics.shaders.Shader
import kotlin.reflect.KProperty

class ShaderManager : Disposable {
    inner class Getter(val file: FileHandle) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file)
    }

    private val shaders = hashMapOf<String, Shader>()

    val names get() = shaders.keys.toList()

    init {
        add("default", DefaultShader)
    }

    fun add(file: FileHandle) {
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

        add(file.fullPath, shader)
    }

    fun add(name: String, shader: Shader) {
        shaders[name] = shader
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in shaders

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(name: String) {
        shaders.remove(name)
    }

    operator fun get(file: FileHandle) = get(file.fullPath)

    operator fun get(name: String): Shader? {
        return shaders[name]
    }

    fun getOrAdd(file: FileHandle): Shader {
        if (file !in this)
            add(file)

        return requireNotNull(this[file])
    }

    operator fun invoke(file: FileHandle) = Getter(file)

    override fun dispose() {
        shaders.forEach { (_, shader) ->
            (shader as? Disposable)?.dispose()
        }
    }
}