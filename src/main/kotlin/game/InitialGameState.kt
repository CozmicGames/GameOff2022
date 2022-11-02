package game

import com.cozmicgames.Kore
import com.cozmicgames.files
import com.cozmicgames.graphics
import com.cozmicgames.graphics.Primitive
import com.cozmicgames.utils.Color
import com.cozmicgames.utils.maths.Vector2
import com.cozmicgames.utils.maths.randomFloat
import engine.Game
import engine.GameState
import engine.graphics.particles.generators.ColorGenerator
import engine.graphics.particles.generators.RandomVelocityGenerator
import engine.graphics.particles.generators.SizeGenerator
import engine.graphics.particles.generators.TimeGenerator
import engine.graphics.particles.spawners.DiskSpawner
import engine.graphics.particles.updaters.*
import engine.graphics.ui.GUI
import engine.graphics.ui.widgets.label
import engine.graphics.ui.widgets.textButton
import engine.scene.Scene
import engine.scene.components.MaterialComponent
import engine.scene.components.ParticleEffectComponent
import engine.scene.components.SpriteComponent
import engine.scene.components.TransformComponent
import engine.scene.processors.RenderProcessor

class InitialGameState : GameState {
    override fun onFrame(delta: Float): GameState {
        Kore.graphics.clear(Color.LIME)
        return this
    }
}
