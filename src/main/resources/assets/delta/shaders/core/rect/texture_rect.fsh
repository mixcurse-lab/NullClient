#version 150

#moj_import <delta:common.glsl>

in vec2 FragPos;
in vec2 FragSize;
in float FragRadius;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;

out vec4 OutColor;

const float RECT_SMOOTHNESS = 0.8;

void main() {
    vec2 center = FragSize * 0.5;
    vec4 radius = vec4(FragRadius);
    float dist = rdist(FragPos, center - 1.0, radius);
    float alpha = 1.0 - smoothstep(1.0 - RECT_SMOOTHNESS, 1.0, dist);
    vec4 color = vec4(1.0, 1.0, 1.0, alpha) * texture(Sampler0, TexCoord) * FragColor;

    if (color.a == 0.0) {
        discard;
    }

    OutColor = color;
}
