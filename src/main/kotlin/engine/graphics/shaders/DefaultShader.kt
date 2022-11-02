package engine.graphics.shaders

object DefaultShader : Shader(
    """
    #section state
    blend add source_alpha one_minus_source_alpha
    
    #section common
    Vertex vertexShader(vec2 position, vec2 texcoord, vec4 color) {
        Vertex v;
        v.position = position;
        v.texcoord = texcoord;
        v.color = color;
        return v;
    }
    
    vec4 fragmentShader(sampler2D sampler, vec2 uv, vec4 color) {
        return texture(sampler, uv) * color;
    }
""".trimIndent()
)
