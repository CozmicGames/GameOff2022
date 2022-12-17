package game.assets

import com.cozmicgames.files.FileHandle
import com.cozmicgames.utils.Disposable
import kotlin.reflect.KProperty

abstract class AssetTypeManager<T : Any, P> : Disposable {
    inner class Getter(private val file: FileHandle, private val name: String, private val params: P) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file, name, params)
    }

    protected abstract val defaultParams: P

    abstract fun add(file: FileHandle, name: String = file.fullPath, params: P = defaultParams)

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    abstract operator fun contains(name: String): Boolean

    fun remove(file: FileHandle) = remove(file.fullPath)

    abstract fun remove(name: String)

    operator fun get(file: FileHandle) = get(file.fullPath)

    abstract operator fun get(name: String): T?

    abstract fun getFileHandle(name: String): FileHandle?

    fun getOrAdd(file: FileHandle, name: String = file.fullPath, params: P = defaultParams): T {
        if (name !in this)
            add(file, name, params)

        return requireNotNull(this[name])
    }

    operator fun invoke(file: FileHandle, name: String = file.fullPath, params: P = defaultParams) = Getter(file, name, params)
}