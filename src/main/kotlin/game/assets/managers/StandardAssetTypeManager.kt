package game.assets.managers

import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.nameWithExtension
import com.cozmicgames.utils.Disposable
import game.assets.AssetTypeManager

abstract class StandardAssetTypeManager<T : Any, P> : AssetTypeManager<T, P>() {
    private inner class Entry(val value: T, val file: FileHandle?)

    protected open val defaultValue: T? = null

    private val entries = hashMapOf<String, Entry>()

    val names get() = entries.keys.toList()

    fun add(name: String, value: T, file: FileHandle? = null) {
        entries[name] = Entry(value, file)
    }

    override operator fun contains(name: String) = name in entries

    override fun remove(name: String) {
        val entry = entries.remove(name)
        if (entry != null) {
            val file = entry.file

            if (file != null) {
                if (file.exists && file.isWritable)
                    file.delete()

                val metaFile = file.sibling("${file.nameWithExtension}.meta")
                if (metaFile.exists && metaFile.isWritable)
                    metaFile.delete()
            }
        }
    }

    override operator fun get(name: String): T? {
        return entries[name]?.value ?: defaultValue
    }

    override fun getFileHandle(name: String) = entries[name]?.file

    override fun dispose() {
        entries.forEach { (_, entry) ->
            (entry.value as? Disposable)?.dispose()
        }
    }
}