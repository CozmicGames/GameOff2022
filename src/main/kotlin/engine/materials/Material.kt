package engine.materials

import com.cozmicgames.utils.*

class Material : Properties() {
    var colorTexturePath by string { "" }
    var shader by string { "default" }

    var restitution by float { 0.0f }
    var staticFriction by float { 0.5f }
    var dynamicFriction by float { 0.3f }
    var density by float { 1.0f }

    val color by color { it.set(Color.WHITE) }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        if (this === other)
            return true

        if (this::class != other::class)
            return false

        other as Material

        if (colorTexturePath != other.colorTexturePath)
            return false

        if (shader != other.shader)
            return false

        if (restitution != other.restitution)
            return false

        if (staticFriction != other.staticFriction)
            return false

        if (dynamicFriction != other.dynamicFriction)
            return false

        if (density != other.density)
            return false

        if (color != other.color)
            return false

        return true
    }
}