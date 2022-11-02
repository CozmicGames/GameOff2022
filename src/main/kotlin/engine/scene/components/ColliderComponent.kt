package engine.scene.components

import com.cozmicgames.utils.Properties
import engine.physics.AxisAlignedRectangleShape
import engine.physics.Body
import engine.physics.RectangleShape
import engine.scene.Component
import engine.scene.processors.PhysicsProcessor

class ColliderComponent : Component() {
    var isStatic = false
        set(value) {
            if (value == field)
                return

            field = value
            recreateBody()
        }

    var body: Body? = null
        private set

    override fun onAdded() {
        recreateBody()
    }

    private fun recreateBody() {
        val physicsProcessor = gameObject.scene.findSceneProcessor<PhysicsProcessor>() ?: return

        this.body?.let {
            physicsProcessor.physics.removeBody(it)
        }

        val transformComponent = gameObject.getOrAddComponent<TransformComponent>()
        val materialComponent = gameObject.getOrAddComponent<MaterialComponent>()

        val body = Body(transformComponent.transform)
        body.setShape(RectangleShape(), materialComponent.material.density)

        if (isStatic)
            body.setStatic()

        body.restitution = materialComponent.material.restitution
        body.staticFriction = materialComponent.material.staticFriction
        body.dynamicFriction = materialComponent.material.dynamicFriction

        physicsProcessor.physics.addBody(body)

        this.body = body
    }

    override fun read(properties: Properties) {
        properties.getBoolean("isStatic")?.let { isStatic = it }
    }

    override fun write(properties: Properties) {
        properties.setBoolean("isStatic", isStatic)
    }
}