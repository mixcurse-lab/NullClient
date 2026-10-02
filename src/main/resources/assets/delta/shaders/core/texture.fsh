#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <delta:ui_common.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec4 vertexColor;
in vec2 localCoord;
in vec2 fragSize;
in float cornerRadius;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord);

    if (cornerRadius > 0.0) {
        vec2 half_ = max(fragSize * 0.5, vec2(0.5));
        float radius = min(cornerRadius, min(half_.x, half_.y));
        float dist = ui_roundedBoxSdfUniform(localCoord, half_, radius);
        float alpha = 1.0 - smoothstep(-1.0, 1.0, dist);
        if (alpha <= 0.0) {
            discard;
        }
        fragColor = vec4(texColor.rgb * vertexColor.rgb, texColor.a * vertexColor.a * alpha);
    } else {
        fragColor = texColor * vertexColor;
    }
}