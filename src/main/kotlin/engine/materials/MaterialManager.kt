package engine.materials

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.readToString
import com.cozmicgames.log
import kotlin.reflect.KProperty

class MaterialManager {
    inner class Getter(private val file: FileHandle, private val name: String) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file, name)
    }

    private val materials = hashMapOf<String, Material>()

    val names get() = materials.keys.toList()

    fun add(file: FileHandle, name: String = file.fullPath) {
        if (!file.exists) {
            Kore.log.error(this::class, "Material file not found: $file")
            return
        }

        val material = try {
            Material().also {
                it.read(file.readToString())
            }
        } catch (e: Exception) {
            Kore.log.error(this::class, "Failed to load material file: $file")
            return
        }

        add(name, material)
    }

    fun add(name: String, material: Material) {
        materials[name] = material
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in materials

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(name: String) {
        materials.remove(name)
    }

    operator fun get(file: FileHandle) = get(file.fullPath)

    operator fun get(name: String): Material? {
        return materials[name]
    }

    fun getOrAdd(file: FileHandle, name: String = file.fullPath): Material {
        if (name !in this)
            add(file, name)

        return requireNotNull(this[name])
    }

    operator fun invoke(file: FileHandle, name: String = file.fullPath) = Getter(file, name)

}