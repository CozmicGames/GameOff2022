package engine.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.audio.Sound
import com.cozmicgames.files.FileHandle
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import kotlin.reflect.KProperty

class SoundManager : Disposable {
    inner class Getter(val file: FileHandle, val name: String) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file, name)
    }

    private class Entry(val value: Sound, val file: FileHandle?)

    private val sounds = hashMapOf<String, Entry>()

    val names get() = sounds.keys.toList()

    fun add(file: FileHandle, name: String = file.fullPath) {
        if (!file.exists) {
            Kore.log.error(this::class, "Sound file not found: $file")
            return
        }

        val sound = Kore.audio.readSound(file)

        if (sound == null) {
            Kore.log.error(this::class, "Failed to load sound file: $file")
            return
        }

        add(name, sound, file)
    }

    fun add(name: String, sound: Sound, file: FileHandle? = null) {
        sounds[name] = Entry(sound, file)
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in sounds

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(name: String) {
        sounds.remove(name)
    }

    operator fun get(file: FileHandle) = get(file.fullPath)

    operator fun get(name: String): Sound? {
        return sounds[name]?.value
    }

    fun getFileHandle(name: String) = sounds[name]?.file

    fun getOrAdd(file: FileHandle, name: String = file.fullPath): Sound {
        if (name !in this)
            add(file, name)

        return requireNotNull(this[name])
    }

    override fun dispose() {
        sounds.forEach { (_, entry) ->
            entry.value.dispose()
        }
    }

    operator fun invoke(file: FileHandle, name: String) = Getter(file, name)
}