#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
in float LineWidth;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

const float TEXT_RADIUS_SCALE = 16.0;

out vec2 TexCoord;
out vec4 FragColor;
flat out ivec2 FragOutlineUV;
out float FragRange;
out float FragThickness;
out float FragOutline;
out float FragOutlineThickness;

void main() {
    TexCoord = UV0;
    FragColor = Color;
    FragOutlineUV = UV1;
    FragRange = float(UV2.x) / TEXT_RADIUS_SCALE;
    FragThickness = Normal.x;
    FragOutline = (LineWidth > 0.001) ? 1.0 : 0.0;
    FragOutlineThickness = LineWidth;

    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
