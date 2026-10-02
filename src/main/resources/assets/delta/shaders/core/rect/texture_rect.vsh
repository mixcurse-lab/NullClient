#version 150

#moj_import <delta:common.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
in float LineWidth;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 FragPos;
out vec2 FragSize;
out float FragRadius;
out vec2 TexCoord;
out vec4 FragColor;

void main() {
    FragPos = -vec2(UV1) / DELTA_SIZE_SCALE;
    FragSize = deltaDecodeSize(UV2);
    FragRadius = max(LineWidth, 0.0);
    TexCoord = UV0;
    FragColor = Color;

    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
