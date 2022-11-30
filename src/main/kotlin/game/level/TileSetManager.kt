package game.level

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.readToString
import com.cozmicgames.log
import com.cozmicgames.utils.Properties
import kotlin.reflect.KProperty

class TileSetManager {
    inner class Getter(val file: FileHandle, val name: String) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file, name)
    }

    private class Entry(val value: TileSet, val file: FileHandle?)

    private val tileSets = hashMapOf<String, Entry>()

    val names get() = tileSets.keys.toList()

    fun add(file: FileHandle, name: String = file.fullPath) {
        if (!file.exists) {
            Kore.log.error(this::class, "Tileset file not found: $file")
            return
        }

        val tileSet = TileSet(name)

        try {
            tileSet.read(Properties().also { it.read(file.readToString()) })
        } catch (e: Exception) {
            Kore.log.error(this::class, "Failed to load tileset file: $file")
            return
        }

        add(name, tileSet, file)
    }

    fun add(name: String, tileSet: TileSet, file: FileHandle? = null) {
        tileSets[name] = Entry(tileSet, file)
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in tileSets

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(name: String) {
        tileSets.remove(name)
    }

    operator fun get(file: FileHandle) = get(file.fullPath)

    operator fun get(name: String): TileSet? {
        return tileSets[name]?.value
    }

    fun getFileHandle(name: String) = tileSets[name]?.file

    fun getOrAdd(file: FileHandle, name: String = file.fullPath): TileSet {
        if (name !in this)
            add(file, name)

        return requireNotNull(this[name])
    }

    operator fun invoke(file: FileHandle, name: String) = Getter(file, name)
}