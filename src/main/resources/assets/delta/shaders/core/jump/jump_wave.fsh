#version 330 core

in vec2 uv;
out vec4 finalColor;

uniform sampler2D SceneSampler;

layout(std140) uniform JumpWaveUniforms {

    vec4 params;
    vec4 crestColor;
};

#define RING_WIDTH 0.30

void main() {
    float ringProgress = params.x;
    float strength = params.y;
    float fade = params.z;


    vec2 centered = (uv - 0.5) * 2.0;
    float dist = length(centered);
    if (dist > 1.0) {
        discard;
    }


    float ringRadius = ringProgress;
    float edge = dist - ringRadius;
    float band = smoothstep(RING_WIDTH, 0.0, abs(edge));
    band *= band;


    float inside = smoothstep(1.0, 0.82, dist);


    float coverage = max(inside, band);
    if (coverage <= 0.001) {
        discard;
    }


    vec2 dir = dist > 0.0001 ? centered / dist : vec2(0.0);

    vec2 screenSize = vec2(textureSize(SceneSampler, 0));
    vec2 screenUv = gl_FragCoord.xy / screenSize;


    float ripple = sin(dist * 38.0 - ringProgress * 26.0) * 0.5 + 0.5;
    float crest = band * mix(0.7, 1.3, ripple);


    float innerDistort = inside * mix(0.55, 1.0, dist);
    float offsetAmount = strength * fade * 0.05 * (innerDistort + crest * 1.6);


    vec2 baseOffset = dir * offsetAmount;
    vec3 refracted = texture(SceneSampler, clamp(screenUv + baseOffset, vec2(0.0), vec2(1.0))).rgb;


    float crestHighlight = pow(band, 1.5) * fade * 0.5;
    float sheen = inside * fade * 0.06;
    vec3 crestTint = crestColor.rgb * pow(band, 1.2) * fade * 0.55;
    vec3 color = refracted + vec3(crestHighlight + sheen) + crestTint;


    float alpha = max(inside, band) * fade;
    finalColor = vec4(color, alpha);
}
