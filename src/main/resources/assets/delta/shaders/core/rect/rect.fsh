#version 150

#moj_import <delta:common.glsl>

in vec2 FragPos;
in vec2 FragSize;
in vec4 FragRadius;
in vec4 FragColor;
in vec3 FragEffectColor;
in float FragEffectValue;
in float FragEffectAlpha;
in float FragMode;

in vec4 FragGradientBottomLeft;
in vec4 FragGradientBottomRight;
in vec4 FragGradientTopRight;

out vec4 fragColor;

const float RECT_SMOOTHNESS = 0.8;

vec4 superSex(vec2 uv, vec4 topLeft, vec4 bottomLeft, vec4 bottomRight, vec4 topRight) {
    vec4 topColor = mix(topLeft, topRight, uv.x);
    vec4 bottomColor = mix(bottomLeft, bottomRight, uv.x);
    return mix(topColor, bottomColor, uv.y);
}

void main() {
    vec2 center = FragSize * 0.5;
    float distOuter = rdist(FragPos, center - 1.0, FragRadius);

    if (FragMode > 0.5 && FragEffectValue < 0.001) {
        vec2 uv = (FragPos + center) / FragSize;
        vec4 gradientColor = superSex(uv, FragColor, FragGradientBottomLeft, FragGradientBottomRight, FragGradientTopRight);

        float alpha = ralpha(FragSize, uv, FragRadius, 1.0);
        vec4 color = vec4(gradientColor.rgb, gradientColor.a * alpha);

        if (color.a == 0.0) {
            discard;
        }
        fragColor = color;
        return;
    }

    if (FragMode > 0.5) {
        float fade = 1.0 - smoothstep(0.0, max(FragEffectValue, 0.5), max(distOuter, 0.0));
        vec4 color = vec4(FragEffectColor, FragEffectAlpha * fade);
        if (color.a == 0.0) {
            discard;
        }
        fragColor = color;
        return;
    }

    float alpha;
    if (FragEffectValue > 0.001) {
        vec2 innerHalf = center - 1.0 - FragEffectValue;
        vec4 radiusInner = max(FragRadius - FragEffectValue, vec4(0.01));
        float distInner = rdist(FragPos, innerHalf, radiusInner);
        float outerFill = 1.0 - smoothstep(1.0 - RECT_SMOOTHNESS, 1.0, distOuter);
        float innerFill = 1.0 - smoothstep(1.0 - RECT_SMOOTHNESS, 1.0, distInner);
        float ringAlpha = outerFill * (1.0 - innerFill);
        float fillAlpha = outerFill * innerFill;

        vec4 fillColor = vec4(FragColor.rgb, FragColor.a * fillAlpha);
        vec4 ringColor = vec4(FragEffectColor, FragEffectAlpha * ringAlpha);
        vec4 color = mix(fillColor, ringColor, ringAlpha);

        if (color.a == 0.0) {
            discard;
        }
        fragColor = color;
        return;
    }

    alpha = 1.0 - smoothstep(1.0 - RECT_SMOOTHNESS, 1.0, distOuter);
    if (FragEffectColor.r > 0.9) {
        alpha = 1.0 - smoothstep(0.0, 1.0, distOuter);
    }
    vec4 color = vec4(FragColor.rgb, FragColor.a * alpha);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}
