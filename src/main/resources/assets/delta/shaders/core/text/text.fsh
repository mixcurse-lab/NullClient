#version 150

in vec2 TexCoord;
in vec4 FragColor;
flat in ivec2 FragOutlineUV;
in float FragRange;
in float FragThickness;
in float FragOutline;
in float FragOutlineThickness;

uniform sampler2D Sampler0;

out vec4 fragColor;

const float TEXT_SMOOTHNESS = 0.5;

float median(vec3 color) {
    return max(min(color.r, color.g), min(max(color.r, color.g), color.b));
}

void main() {
    float dist = median(texture(Sampler0, TexCoord).rgb) - 0.5 + FragThickness;
    vec2 h = vec2(dFdx(TexCoord.x), dFdy(TexCoord.y)) * textureSize(Sampler0, 0);
    float pixels = FragRange * inversesqrt(h.x * h.x + h.y * h.y);
    float alpha = smoothstep(-TEXT_SMOOTHNESS, TEXT_SMOOTHNESS, dist * pixels);
    vec4 color = vec4(FragColor.rgb, FragColor.a * alpha);

    if (FragOutline > 0.5) {
        vec4 outlineColor = vec4(
            float((FragOutlineUV.x & 0xFF)) / 255.0,
            float(((FragOutlineUV.x >> 8) & 0xFF)) / 255.0,
            float((FragOutlineUV.y & 0xFF)) / 255.0,
            float(((FragOutlineUV.y >> 8) & 0xFF)) / 255.0
        );
        float outlineAlpha = smoothstep(-TEXT_SMOOTHNESS, TEXT_SMOOTHNESS, (dist + FragOutlineThickness) * pixels);
        color = mix(outlineColor, FragColor, alpha);
        color.a *= outlineAlpha;
    }

    fragColor = color;
}
