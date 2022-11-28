package game.level

import com.cozmicgames.Kore
import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.readToString
import com.cozmicgames.log
import com.cozmicgames.utils.Properties
import kotlin.reflect.KProperty

class TileSetManager {
    companion object {
        private val EMPTY = TileSet()
    }

    inner class Getter(val file: FileHandle, val name: String) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file, name)
    }

    private val tileSets = hashMapOf<String, TileSet>()

    val names get() = tileSets.keys.toList()

    fun add(file: FileHandle, name: String = file.fullPath) {
        if (!file.exists) {
            Kore.log.error(this::class, "Tileset file not found: $file")
            return
        }

        val tileSet = TileSet()

        try {
            tileSet.read(Properties().also { it.read(file.readToString()) })
        } catch (e: Exception) {
            Kore.log.error(this::class, "Failed to load tileset file: $file")
            return
        }

        add(name, tileSet)
    }

    fun add(name: String, tileSet: TileSet) {
        tileSets[name] = tileSet
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in tileSets

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(name: String) {
        tileSets.remove(name)
    }

    operator fun get(file: FileHandle) = get(file.fullPath)

    operator fun get(name: String): TileSet {
        return tileSets[name] ?: EMPTY
    }

    fun getOrAdd(file: FileHandle, name: String = file.fullPath): TileSet {
        if (name !in this)
            add(file, name)

        return this[name]
    }

    operator fun invoke(file: FileHandle, name: String) = Getter(file, name)
}