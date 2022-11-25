package engine.audio

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.audio.Sound
import com.cozmicgames.files.FileHandle
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import kotlin.reflect.KProperty

class SoundManager : Disposable {
    inner class Getter(val file: FileHandle) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = getOrAdd(file)
    }

    private val sounds = hashMapOf<String, Sound>()

    val names get() = sounds.keys.toList()

    fun add(file: FileHandle) {
        if (!file.exists) {
            Kore.log.error(this::class, "Sound file not found: $file")
            return
        }

        val sound = Kore.audio.readSound(file)

        if (sound == null) {
            Kore.log.error(this::class, "Failed to load sound file: $file")
            return
        }

        add(file.fullPath, sound)
    }

    fun add(name: String, sound: Sound) {
        sounds[name] = sound
    }

    operator fun contains(file: FileHandle) = contains(file.fullPath)

    operator fun contains(name: String) = name in sounds

    fun remove(file: FileHandle) = remove(file.fullPath)

    fun remove(name: String) {
        sounds.remove(name)
    }

    operator fun get(file: FileHandle) = get(file.fullPath)

    operator fun get(name: String): Sound? {
        return sounds[name]
    }

    fun getOrAdd(file: FileHandle): Sound {
        if (file !in this)
            add(file)

        return requireNotNull(this[file])
    }

    override fun dispose() {
        sounds.forEach { (_, sound) ->
            sound.dispose()
        }
    }

    operator fun invoke(file: FileHandle) = Getter(file)
}