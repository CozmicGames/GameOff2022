package game.assets.managers

import com.cozmicgames.Kore
import com.cozmicgames.audio
import com.cozmicgames.audio.Sound
import com.cozmicgames.files.FileHandle
import com.cozmicgames.log

class SoundManager : StandardAssetTypeManager<Sound, Unit>() {
    override val defaultParams = Unit

    override fun add(file: FileHandle, name: String, params: Unit) {
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
}