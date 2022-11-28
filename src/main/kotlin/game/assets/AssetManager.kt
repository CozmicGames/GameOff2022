package game.assets

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.FileHandle
import com.cozmicgames.utils.Disposable
import game.assets.types.*

class AssetManager : Disposable {
    private val registeredAssetTypes = hashSetOf<AssetType<*>>()

    val assetTypes get() = registeredAssetTypes.toList()

    init {
        registerAssetType(TextureAssetType())
        registerAssetType(FontAssetType())
        registerAssetType(SoundAssetType())
        registerAssetType(MaterialAssetType())
        registerAssetType(TileSetAssetType())
        registerAssetType(ShaderAssetType())
    }

    fun getAssetFileHandle(name: String) = Kore.files.local("assets/$name")

    fun registerAssetType(assetType: AssetType<*>): AssetType<*> {
        registeredAssetTypes.add(assetType)
        return assetType
    }

    fun findAssetType(predicate: (AssetType<*>) -> Boolean) = registeredAssetTypes.find(predicate)

    fun findOrRegisterAssetType(predicate: (AssetType<*>) -> Boolean, block: () -> AssetType<*>) = findAssetType(predicate) ?: registerAssetType(block())

    fun importFile(file: FileHandle, destFile: FileHandle): FileHandle {
        file.copyTo(destFile)
        return destFile
    }

    override fun dispose() {
        registeredAssetTypes.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}

fun AssetManager.findAssetType(name: String) = findAssetType { it.name == name }

fun AssetManager.findOrRegisterAssetType(name: String, block: () -> AssetType<*>) = findOrRegisterAssetType({ it.name == name }, block)
