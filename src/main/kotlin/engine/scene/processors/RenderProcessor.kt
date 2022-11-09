package engine.scene.processors

import engine.Game
import engine.scene.SceneProcessor
import engine.scene.components.*

class RenderProcessor : SceneProcessor() {
    override fun shouldProcess(delta: Float) = true

    override fun process(delta: Float) {
        val scene = scene ?: return

        for (gameObject in scene.activeGameObjects) {
            val spriteComponent = gameObject.getComponent<SpriteComponent>()

            val transformComponent = gameObject.getComponent<TransformComponent>()
            val particleEffectComponent = gameObject.getComponent<ParticleEffectComponent>()

            if (spriteComponent != null)
                Game.renderer.submit(spriteComponent.layer, spriteComponent, spriteComponent.material?.let { Game.materials[it] } ?: Game.graphics2d.missingMaterial, spriteComponent.isFlippedX, spriteComponent.isFlippedY)

            if (transformComponent != null && particleEffectComponent != null)
                particleEffectComponent.effect.render(particleEffectComponent.layer, transformComponent.transform.global)
        }
    }
}