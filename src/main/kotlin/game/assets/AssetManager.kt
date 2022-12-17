package game.assets

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.files.*
import com.cozmicgames.log
import com.cozmicgames.utils.Disposable
import game.assets.types.*
import kotlin.reflect.KClass

class AssetManager : Disposable {
    private val registeredAssetTypes = hashSetOf<AssetType<*>>()
    private val managers = hashMapOf<Any, AssetTypeManager<*, *>>()

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

    inline fun <reified T : Any> registerAssetTypeManager(manager: AssetTypeManager<T, *>) = registerAssetTypeManager(T::class, manager)

    fun <T : Any> registerAssetTypeManager(type: KClass<T>, manager: AssetTypeManager<T, *>) {
        managers.put(type, manager)?.dispose()
    }

    inline fun <reified T : Any> getAssetTypeManager() = getAssetTypeManager(T::class)

    fun <T : Any> getAssetTypeManager(type: KClass<T>) = managers[type] as? AssetTypeManager<T, *>

    fun <T : Any> getAsset(name: String, type: KClass<T>): T? {
        val manager = getAssetTypeManager(type) ?: return null
        return manager[name]
    }

    fun load(file: FileHandle) {
        val type = findAssetType { file.extension in it.supportedFormats } ?: return
        load(file, type)
    }

    fun load(file: FileHandle, typeName: String) {
        val type = findAssetType(typeName) ?: return
        load(file, type)
    }

    fun load(file: FileHandle, type: AssetType<*>) {
        Kore.log.info(this::class, "Loading asset (${type.name}): $file")
        type.load(file)
    }

    fun createZipArchive() {
        val zipFile = Kore.files.local("assets.zip")

        if (zipFile.exists)
            zipFile.delete()

        zipFile.buildZip {
            fun addDirectory(directoryFile: FileHandle) {
                directoryFile.list {
                    val file = directoryFile.child(it)
                    if (file.isDirectory)
                        addDirectory(file)
                    else {
                        val name = file.fullPath.removePrefix("${directoryFile.fullPath}/")
                        val content = file.readToBytes()

                        addFile(name, content)
                    }
                }
            }

            addDirectory(Kore.files.local("assets"))
        }
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
