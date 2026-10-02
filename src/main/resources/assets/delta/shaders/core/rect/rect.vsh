#version 150

#moj_import <delta:common.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec4 Normal;
in float LineWidth;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 FragPos;
out vec2 FragSize;
out vec4 FragRadius;
out vec4 FragColor;
out vec3 FragEffectColor;
out float FragEffectValue;
out float FragEffectAlpha;
out float FragMode;

out vec4 FragGradientBottomLeft;
out vec4 FragGradientBottomRight;
out vec4 FragGradientTopRight;

void main() {
    vec2 size = deltaDecodeSize(UV1);

    float effectAlpha;
    float mode;
    vec2 effect = deltaDecodeEffectZ(Position.z, effectAlpha, mode);

    FragRadius = deltaDecodeRadius(UV2, LineWidth);

    FragSize = size;
    FragColor = Color;
    FragEffectValue = effect.x;
    FragEffectAlpha = effectAlpha;
    FragMode = mode;

    if (mode > 0.5 && effect.x < 0.001 && effectAlpha < 0.5) {
        vec2 localUV = rvertexcoord(gl_VertexID);
        FragPos = (localUV - 0.5) * size;
        FragEffectColor = vec3(0.0);
        FragGradientBottomLeft = vec4(Normal * 0.5 + 0.5, 1.0);

        int brVal = int(UV0.x + 0.5);
        int trVal = int(UV0.y + 0.5);
        FragGradientBottomRight = vec4(
            float((brVal >> 16) & 0xFF) / 255.0,
            float((brVal >> 8) & 0xFF) / 255.0,
            float(brVal & 0xFF) / 255.0,
            1.0
        );
        FragGradientTopRight = vec4(
            float((trVal >> 16) & 0xFF) / 255.0,
            float((trVal >> 8) & 0xFF) / 255.0,
            float(trVal & 0xFF) / 255.0,
            1.0
        );
    } else {
        FragPos = -UV0;
        FragEffectColor = Normal.rgb * 0.5 + 0.5;

        FragGradientBottomLeft = vec4(0.0);
        FragGradientBottomRight = vec4(0.0);
        FragGradientTopRight = vec4(0.0);
    }

    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
