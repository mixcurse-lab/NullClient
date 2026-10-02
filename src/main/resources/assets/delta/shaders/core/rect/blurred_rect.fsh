#version 330

#moj_import <delta:ui_common.glsl>

in vec2 FragCoord;
in vec2 FragSize;
in vec4 FragRadius;
in vec4 FragColor;
in float FragMix;
in float FragSmoothness;
in float FragAlphaMul;
in vec2 TexCoord;

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    vec2 halfSize = FragSize * 0.5;
    vec2 center = halfSize;
    vec4 radii = ui_fitRadii(FragRadius, max(FragSize - 2.0, vec2(0.01)));
    vec2 fragPos = center - (FragCoord * FragSize);
    float dist = ui_roundedBoxSdf(fragPos, center - 1.0, radii);
    float smoothedAlpha = 1.0 - smoothstep(1.0 - FragSmoothness, 1.0, dist);
    vec4 texColor = texture(Sampler0, TexCoord);
    vec4 mixedColor = mix(texColor, FragColor, FragMix);
    mixedColor.a = smoothedAlpha * FragAlphaMul;
    if (mixedColor.a <= 0.0) {
        discard;
    }
    fragColor = mixedColor;
}
