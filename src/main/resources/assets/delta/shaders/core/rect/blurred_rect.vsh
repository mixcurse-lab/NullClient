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

out vec2 FragCoord;
out vec2 FragSize;
out vec4 FragRadius;
out vec4 FragColor;
out float FragMix;
out float FragSmoothness;
out float FragAlphaMul;
out vec2 TexCoord;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position.xy, 0.0, 1.0);

    FragCoord = UV0;
    FragSize = vec2(UV1) / UI_SIZE_SCALE;
    vec2 bottomRadii = ui_unpackDual12(LineWidth);
    FragRadius = vec4(vec2(UV2) / UI_RADIUS_SCALE, bottomRadii.x, bottomRadii.y);
    FragColor = Color;
    FragMix = Normal.r * 0.5 + 0.5;
    FragSmoothness = Normal.g * 0.5 + 0.5;
    FragAlphaMul = Normal.b * 0.5 + 0.5;

    TexCoord = gl_Position.xy / gl_Position.w * 0.5 + 0.5;
}
