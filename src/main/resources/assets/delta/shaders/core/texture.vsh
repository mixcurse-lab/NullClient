#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <delta:ui_common.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
in float LineWidth;

out vec2 texCoord;
out vec4 vertexColor;
out vec2 localCoord;
out vec2 fragSize;
out float cornerRadius;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord = UV0;
    vertexColor = Color;
    localCoord = vec2(UV1) / UI_SIZE_SCALE;
    fragSize = vec2(UV2) / UI_SIZE_SCALE;
    cornerRadius = LineWidth;
}