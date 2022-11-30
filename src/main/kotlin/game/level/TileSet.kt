package game.level

import com.cozmicgames.utils.Disposable
import com.cozmicgames.utils.Properties
import com.cozmicgames.utils.UUID
import com.cozmicgames.utils.extensions.enumValueOfOrNull
import com.cozmicgames.utils.extensions.pathWithoutExtension
import engine.Game
import engine.materials.Material
import game.components.GridComponent
import game.components.getCellType

class TileSet(val name: String) : Disposable {
    companion object {
        private fun createMaterial(tileSetName: String): String {
            val name = "${tileSetName.pathWithoutExtension}/${UUID.randomUUID()}.material"
            val material = Material()
            material.colorTexturePath = "internal/images/empty_tiletype.png"
            Game.materials.add(name, material)
            return name
        }
    }

    class TileType(val tileSet: TileSet) : Disposable {
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

        inner class Rule : Disposable {
            val material = createMaterial(tileSet.name)

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

                properties.getProperties("material")?.let {
                    val material = Game.materials[this.material]
                    material?.clear()
                    material?.set(it)
                }

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
                properties.setProperties("material", Game.materials[material] ?: Material().also {
                    it.colorTexturePath = "assets/internal/images/empty_tiletype.png"
                })

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

            override fun dispose() {
                Game.materials.remove(material)
            }
        }

        private val rulesInternal = arrayListOf<Rule>()

        val rules get() = ArrayList(rulesInternal).toList()

        var defaultMaterial = createMaterial(tileSet.name)
        var width = 1.0f
        var height = 1.0f

        fun getMaterial(gridComponent: GridComponent, cellX: Int, cellY: Int): String {
            if (rulesInternal.isEmpty())
                return defaultMaterial

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

        fun removeRule(rule: Rule) {
            if (rulesInternal.remove(rule))
                rule.dispose()
        }

        fun read(properties: Properties) {
            rulesInternal.clear()

            properties.getProperties("defaultMaterial")?.let {
                val material = Game.materials[defaultMaterial]
                material?.clear()
                material?.set(it)
            }

            properties.getPropertiesArray("rules")?.let {
                it.forEach {
                    val rule = addRule()
                    rule.read(it)
                }
            }
        }

        fun write(properties: Properties) {
            val rulesProperties = arrayListOf<Properties>()

            rulesInternal.forEach {
                val ruleProperties = Properties()
                it.write(ruleProperties)
                rulesProperties += ruleProperties
            }

            properties.setProperties("defaultMaterial", Game.materials[defaultMaterial] ?: (Material().also {
                it.colorTexturePath = "internal/images/empty_tiletype.png"
            }))

            properties.setPropertiesArray("rules", rulesProperties.toTypedArray())
        }

        override fun dispose() {
            Game.materials.remove(defaultMaterial)

            rulesInternal.forEach {
                it.dispose()
            }
        }
    }

    private val types = hashMapOf<String, TileType>()

    val tileTypeNames get() = types.keys.toList()

    operator fun get(id: String) = types[id]

    operator fun contains(id: String) = id in types

    fun addType(): String {
        val id = UUID.randomUUID().toString()
        this[id] = TileType(this)
        return id
    }

    operator fun set(name: String, type: TileType) {
        types[name] = type
    }

    fun remove(name: String): Boolean {
        val type = types.remove(name)
        return if (type != null) {
            type.dispose()
            true
        } else
            false
    }

    fun clear() {
        types.clear()
    }

    fun set(tileSet: TileSet) {
        tileSet.types.forEach { name, type ->
            this[name] = TileType(this).also { dest ->
                dest.defaultMaterial = type.defaultMaterial
                dest.width = type.width
                dest.height = type.height

                type.rules.forEach {
                    val rule = dest.addRule()

                    Game.materials[it.material]?.let {
                        Game.materials[rule.material]?.set(it)
                    }

                    fun copyDependency(src: TileType.Dependency?) = when (src?.type) {
                        TileType.Dependency.Type.EMPTY -> TileType.EmptyDependency
                        TileType.Dependency.Type.SOLID -> TileType.EmptyDependency
                        TileType.Dependency.Type.TILE -> TileType.TileTypeDependency().also {
                            it.tileTypes.addAll((src as TileType.TileTypeDependency).tileTypes)
                        }
                        else -> null
                    }

                    rule.dependencyTopLeft = copyDependency(it.dependencyTopLeft)
                    rule.dependencyTopCenter = copyDependency(it.dependencyTopCenter)
                    rule.dependencyTopRight = copyDependency(it.dependencyTopRight)
                    rule.dependencyCenterLeft = copyDependency(it.dependencyCenterLeft)
                    rule.dependencyCenterRight = copyDependency(it.dependencyCenterRight)
                    rule.dependencyBottomLeft = copyDependency(it.dependencyBottomLeft)
                    rule.dependencyBottomCenter = copyDependency(it.dependencyBottomCenter)
                    rule.dependencyBottomRight = copyDependency(it.dependencyBottomRight)
                }
            }
        }
    }

    fun read(properties: Properties) {
        types.clear()

        properties.getPropertiesArray("types")?.let {
            for (typeProperties in it) {
                val name = typeProperties.getString("name") ?: continue
                val tileType = TileType(this)
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
            typeProperties.setFloat("width", tileType.width)
            typeProperties.setFloat("height", tileType.height)
            tileType.write(typeProperties)

            typesProperties += typeProperties
        }

        properties.setPropertiesArray("types", typesProperties.toTypedArray())
    }

    override fun dispose() {
        types.forEach { (_, type) ->
            type.dispose()
        }
    }
}