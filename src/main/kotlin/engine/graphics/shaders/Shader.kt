package engine.graphics.shaders

import com.cozmicgames.files.FileHandle
import com.cozmicgames.files.readToString
import com.cozmicgames.graphics.gpu.VertexLayout
import com.cozmicgames.graphics.gpu.pipeline.PipelineDefinition
import com.cozmicgames.utils.extensions.removeComments
import engine.materials.Material

open class Shader(source: String) {
    companion object {
        private fun loadSource(file: FileHandle): String {
            val text = file.readToString()
            return text.removeComments()
        }

        val VERTEX_LAYOUT = VertexLayout {
            vec2("position")
            vec2("texcoord")
            vec2("uBounds")
            vec2("vBounds")
            vec4("color", true, VertexLayout.AttributeType.BYTE)
        }
    }

    constructor(file: FileHandle) : this(loadSource(file))

    private val definition = PipelineDefinition()

    init {
        definition.parse(
            """
            $source
                
            #section layout
            vec2 position
            vec2 texcoord
            vec2 uBounds
            vec2 vBounds
            normalized vec4 color byte
            
            #section uniforms
            mat4 uCameraTransform;
            bool uFlipX;
            bool uFlipY;
            sampler2D uTexture;
            bool uUseSamplePointAntialiased;
            
            #section types
            struct Vertex {
                vec2 position;
                vec2 texcoord;
                vec4 color;
            }
            
            #section vertex
            #define uMin (uBounds.x)
            #define vMin (vBounds.x)
            #define uMax (uBounds.y)
            #define vMax (vBounds.y)
            #define uRange (uMax - uMin)
            #define vRange (vMax - vMin)

            out vec2 vTexcoord;
            out vec4 vColor;

            void main() {
                vec2 tc;
                
                if (uFlipX) {
                    float normalizedU = (texcoord.x - uMin) / uRange;
                    float flippedNormalizedU = 1.0 - normalizedU;
                    float flippedU = uMin + flippedNormalizedU * uRange;
                    tc.x = flippedU;
                } else
                    tc.x = texcoord.x;
                
                if (uFlipY) {
                    float normalizedV = (texcoord.y - vMin) / vRange;
                    float flippedNormalizedV = 1.0 - normalizedV;
                    float flippedV = vMin + flippedNormalizedV * vRange;
                    tc.y = flippedV;
                } else
                    tc.y = texcoord.y;
                
               
                Vertex v = vertexShader(position, tc, color);
                 
                gl_Position = uCameraTransform * vec4(v.position, 0.0, 1.0);
                vTexcoord = v.texcoord;
                vColor = v.color;
            }
            
            #section fragment
            in vec2 vTexcoord;
            in vec4 vColor;
                                
            out vec4 outColor;
            
            void main() {
                outColor = fragmentShader(uTexture, vTexcoord, vColor);
            }
            
        """.trimIndent()
        )
    }

    fun createPipeline() = definition.createPipeline()

    open fun setMaterial(material: Material) {}
}