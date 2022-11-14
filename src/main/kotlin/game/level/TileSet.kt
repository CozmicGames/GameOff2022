package game.level

import com.cozmicgames.utils.Properties

class TileSet {
    companion object {
        private val MISSING_TILETYPE = TileType("<missing>", 1.0f, 1.0f)
    }

    data class TileType(val material: String, val width: Float, val height: Float)

    private val types = hashMapOf<String, TileType>()

    operator fun get(name: String) = types[name] ?: MISSING_TILETYPE

    operator fun set(name: String, type: TileType) {
        types[name] = type
    }

    fun read(properties: Properties) {
        types.clear()

        properties.getPropertiesArray("types")?.let {
            for (typeProperties in it) {
                val name = typeProperties.getString("name") ?: continue
                val texture = typeProperties.getString("material") ?: continue
                val width = typeProperties.getFloat("width") ?: 1.0f
                val height = typeProperties.getFloat("height") ?: 1.0f

                types[name] = TileType(texture, width, height)
            }
        }
    }

    fun write(properties: Properties) {
        val typesProperties = arrayListOf<Properties>()

        types.forEach { name, type ->
            val typeProperties = Properties()

            typeProperties.setString("name", name)
            typeProperties.setString("material", type.material)
            typeProperties.setFloat("width", type.width)
            typeProperties.setFloat("height", type.height)

            typesProperties += typeProperties
        }

        properties.setPropertiesArray("types", typesProperties.toTypedArray())
    }
}