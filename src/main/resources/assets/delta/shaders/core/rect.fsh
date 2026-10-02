#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <delta:ui_common.glsl>
#moj_import <delta:ui_fragment.glsl>

in vec2 localPos;
in vec4 fillColor;
flat in vec2 halfSize;
flat in vec4 cornerRadii;
flat in vec4 effectColor;
flat in float effectValue;
flat in float shadowMode;

out vec4 fragColor;

void main() {
    vec2 half_ = max(halfSize - vec2(1.0), vec2(0.5));
    vec4 radii = ui_fitRadii(cornerRadii, half_ * 2.0);
    float dist = ui_roundedBoxSdf(localPos, half_, radii);

    if (shadowMode > 0.5) {
        float shadowAlpha = effectColor.a * ui_gaussFalloff(dist, max(effectValue, 0.5));
        if (shadowAlpha <= 0.002) {
            discard;
        }
        fragColor = vec4(effectColor.rgb, shadowAlpha);
        return;
    }

    if (effectColor.r > 0.9 && effectValue < 0.001) {
        float alpha = 1.0 - smoothstep(0.0, 1.0, dist);
        float fillAlpha = fillColor.a * alpha;
        if (fillAlpha <= 0.0) {
            discard;
        }
        fragColor = vec4(fillColor.rgb, fillAlpha);
        return;
    }

    float outerCoverage = 1.0 - smoothstep(0.0, 1.0, dist);
    float borderThickness = clamp(effectValue, 0.0, min(half_.x, half_.y));
    float innerCoverage = borderThickness > 0.0 ? 1.0 - smoothstep(0.0, 1.0, dist + borderThickness) : outerCoverage;
    if (outerCoverage <= 0.0) {
        discard;
    }

    float fillAlpha = fillColor.a * innerCoverage;
    float borderAlpha = effectColor.a * max(outerCoverage - innerCoverage, 0.0);

    vec3 fillCol = fillColor.rgb * fillAlpha;
    vec3 borderCol = effectColor.rgb * borderAlpha;

    vec3 result = fillCol + borderCol * (1.0 - fillAlpha);
    float resultAlpha = fillAlpha + borderAlpha * (1.0 - fillAlpha);

    if (resultAlpha <= 0.0) {
        discard;
    }
    fragColor = vec4(result / resultAlpha, resultAlpha);
}
