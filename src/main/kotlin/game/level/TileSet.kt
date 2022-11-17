package game.level

import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.UUID
import com.cozmicgames.utils.extensions.enumValueOfOrNull
import game.components.GridComponent
import game.components.getCellType

class TileSet {
    companion object {
        private val MISSING_TILETYPE = SimpleTileType()
    }

    sealed class TileType(val type: Type) {
        enum class Type {
            SIMPLE,
            RULE
        }

        var width = 1.0f
        var height = 1.0f

        abstract val defaultMaterial: String

        abstract fun getMaterial(gridComponent: GridComponent, cellX: Int, cellY: Int): String

        abstract fun read(properties: Properties)

        abstract fun write(properties: Properties)
    }

    class SimpleTileType : TileType(Type.SIMPLE) {
        var material = "assets/materials/empty_tiletype.material"

        override val defaultMaterial get() = material

        override fun getMaterial(gridComponent: GridComponent, cellX: Int, cellY: Int) = material

        override fun read(properties: Properties) {
            properties.getString("material")?.let { material = it }
        }

        override fun write(properties: Properties) {
            properties.setString("material", material)
        }
    }

    class RuleTileType : TileType(Type.RULE) {
        sealed class Dependency(val type: Type) {
            enum class Type {
                SOLID,
                EMPTY,
                TILE
            }
        }

        object SolidDependency : Dependency(Type.SOLID)

        object EmptyDependency : Dependency(Type.EMPTY)

        class TileTypeDependency : Dependency(Type.TILE) {
            var tileTypes = arrayListOf<String>()
        }

        class Rule {
            var material = "assets/materials/empty_tiletype.material"

            var dependencyTopLeft: Dependency? = null
            var dependencyTopCenter: Dependency? = null
            var dependencyTopRight: Dependency? = null
            var dependencyCenterLeft: Dependency? = null
            var dependencyCenterRight: Dependency? = null
            var dependencyBottomLeft: Dependency? = null
            var dependencyBottomCenter: Dependency? = null
            var dependencyBottomRight: Dependency? = null

            fun read(properties: Properties) {
                dependencyTopLeft = null
                dependencyTopCenter = null
                dependencyTopRight = null
                dependencyCenterLeft = null
                dependencyCenterRight = null
                dependencyBottomLeft = null
                dependencyBottomCenter = null
                dependencyBottomRight = null

                properties.getString("material")?.let { material = it }

                fun readDependencyProperties(properties: Properties): Dependency? {
                    val typeName = properties.getString("type") ?: return null
                    val type = enumValueOfOrNull<Dependency.Type>(typeName) ?: return null

                    return when (type) {
                        Dependency.Type.SOLID -> SolidDependency
                        Dependency.Type.EMPTY -> EmptyDependency
                        Dependency.Type.TILE -> {
                            TileTypeDependency().also { dependency ->
                                properties.getStringArray("tileTypes")?.let { dependency.tileTypes.addAll(it) }
                            }
                        }
                    }
                }

                properties.getProperties("topLeft")?.let { dependencyTopLeft = readDependencyProperties(it) }
                properties.getProperties("topCenter")?.let { dependencyTopCenter = readDependencyProperties(it) }
                properties.getProperties("topRight")?.let { dependencyTopRight = readDependencyProperties(it) }
                properties.getProperties("centerLeft")?.let { dependencyCenterLeft = readDependencyProperties(it) }
                properties.getProperties("centerRight")?.let { dependencyCenterRight = readDependencyProperties(it) }
                properties.getProperties("bottomLeft")?.let { dependencyBottomLeft = readDependencyProperties(it) }
                properties.getProperties("bottomCenter")?.let { dependencyBottomCenter = readDependencyProperties(it) }
                properties.getProperties("bottomRight")?.let { dependencyBottomRight = readDependencyProperties(it) }
            }

            fun write(properties: Properties) {
                properties.setString("material", material)

                fun writeDependencyProperties(dependency: Dependency?): Properties {
                    val dependencyProperties = Properties()
                    dependency?.let {
                        dependencyProperties.setString("type", it.type.name)
                        if (it is TileTypeDependency)
                            properties.setStringArray("tileTypes", it.tileTypes.toTypedArray())
                    }
                    return dependencyProperties
                }

                properties.setProperties("topLeft", writeDependencyProperties(dependencyTopLeft))
                properties.setProperties("topCenter", writeDependencyProperties(dependencyTopCenter))
                properties.setProperties("topRight", writeDependencyProperties(dependencyTopRight))
                properties.setProperties("centerLeft", writeDependencyProperties(dependencyCenterLeft))
                properties.setProperties("centerRight", writeDependencyProperties(dependencyCenterRight))
                properties.setProperties("bottomLeft", writeDependencyProperties(dependencyBottomLeft))
                properties.setProperties("bottomCenter", writeDependencyProperties(dependencyBottomCenter))
                properties.setProperties("bottomRight", writeDependencyProperties(dependencyBottomRight))
            }
        }

        override var defaultMaterial = "assets/materials/empty_tiletype.material"

        private val rulesInternal = arrayListOf<Rule>()

        val rules get() = ArrayList(rulesInternal).asIterable()

        override fun getMaterial(gridComponent: GridComponent, cellX: Int, cellY: Int): String {
            val tileTypeTopLeft = gridComponent.getCellType(cellX - 1, cellY + 1)
            val tileTypeTopCenter = gridComponent.getCellType(cellX, cellY + 1)
            val tileTypeTopRight = gridComponent.getCellType(cellX + 1, cellY + 1)
            val tileTypeCenterLeft = gridComponent.getCellType(cellX - 1, cellY)
            val tileTypeCenterRight = gridComponent.getCellType(cellX + 1, cellY)
            val tileTypeBottomLeft = gridComponent.getCellType(cellX - 1, cellY - 1)
            val tileTypeBottomCenter = gridComponent.getCellType(cellX, cellY - 1)
            val tileTypeBottomRight = gridComponent.getCellType(cellX + 1, cellY - 1)

            fun checkDependency(dependency: Dependency?, tileType: String?): Boolean {
                if (dependency == null)
                    return true

                return when (dependency.type) {
                    Dependency.Type.EMPTY -> tileType == null
                    Dependency.Type.SOLID -> tileType != null
                    Dependency.Type.TILE -> tileType in (dependency as TileTypeDependency).tileTypes
                }
            }

            var material = defaultMaterial

            for (rule in rulesInternal) {
                if (!checkDependency(rule.dependencyTopLeft, tileTypeTopLeft))
                    continue

                if (!checkDependency(rule.dependencyTopCenter, tileTypeTopCenter))
                    continue

                if (!checkDependency(rule.dependencyTopRight, tileTypeTopRight))
                    continue

                if (!checkDependency(rule.dependencyCenterLeft, tileTypeCenterLeft))
                    continue

                if (!checkDependency(rule.dependencyCenterRight, tileTypeCenterRight))
                    continue

                if (!checkDependency(rule.dependencyBottomLeft, tileTypeBottomLeft))
                    continue

                if (!checkDependency(rule.dependencyBottomCenter, tileTypeBottomCenter))
                    continue

                if (!checkDependency(rule.dependencyBottomRight, tileTypeBottomRight))
                    continue

                material = rule.material
                break
            }

            return material
        }

        fun addRule(): Rule {
            val rule = Rule()
            rulesInternal += rule
            return rule
        }

        fun remove(rule: Rule) {
            rulesInternal -= rule
        }

        override fun read(properties: Properties) {
            rulesInternal.clear()

            properties.getString("defaultMaterial")?.let { defaultMaterial = it }

            properties.getPropertiesArray("rules")?.let {
                it.forEach {
                    val rule = addRule()
                    rule.read(it)
                }
            }
        }

        override fun write(properties: Properties) {
            val rulesProperties = arrayListOf<Properties>()

            rulesInternal.forEach {
                val ruleProperties = Properties()
                it.write(ruleProperties)
                rulesProperties += ruleProperties
            }

            properties.setString("defaultMaterial", defaultMaterial)
            properties.setPropertiesArray("rules", rulesProperties.toTypedArray())
        }
    }

    private val types = hashMapOf<String, TileType>()

    val ids get() = types.keys.asIterable()

    operator fun get(id: String) = types[id] ?: MISSING_TILETYPE

    operator fun contains(id: String) = id in types

    fun addType(): String {
        val id = UUID.randomUUID().toString()
        this[id] = SimpleTileType()
        return id
    }

    operator fun set(name: String, type: TileType) {
        types[name] = type
    }

    fun read(properties: Properties) {
        types.clear()

        properties.getPropertiesArray("types")?.let {
            for (typeProperties in it) {
                val name = typeProperties.getString("name") ?: continue
                val typeName = typeProperties.getString("type") ?: continue
                val type = enumValueOfOrNull<TileType.Type>(typeName) ?: continue

                val tileType = when (type) {
                    TileType.Type.SIMPLE -> SimpleTileType()
                    TileType.Type.RULE -> RuleTileType()
                }

                tileType.width = typeProperties.getFloat("width") ?: 1.0f
                tileType.height = typeProperties.getFloat("height") ?: 1.0f
                tileType.read(typeProperties)
                types[name] = tileType
            }
        }
    }

    fun write(properties: Properties) {
        val typesProperties = arrayListOf<Properties>()

        types.forEach { name, tileType ->
            val typeProperties = Properties()

            typeProperties.setString("name", name)
            typeProperties.setString("type", tileType.type.name)
            typeProperties.setFloat("width", tileType.width)
            typeProperties.setFloat("height", tileType.height)
            tileType.write(typeProperties)

            typesProperties += typeProperties
        }

        properties.setPropertiesArray("types", typesProperties.toTypedArray())
    }
}