#version 330 core

layout(std140) uniform GridParams {
    vec2 uResolution;
    vec2 uCursor;
    vec4 uMisc;
    vec4 uWells[32];
    vec4 uAccentTop;
    vec4 uAccentBottom;
};

out vec4 fragColor;

float sat(float v) {
    return clamp(v, 0.0, 1.0);
}

float lineGrid(vec2 p, float spacing, float width) {
    vec2 cell = abs(fract(p / spacing) - 0.5) * spacing;
    float d = min(cell.x, cell.y);
    return 1.0 - smoothstep(width, width + 1.35, d);
}

void main() {
    vec2 pixel = vec2(gl_FragCoord.x, uResolution.y - gl_FragCoord.y);
    vec2 warped = pixel;
    float reveal = 1.0 - exp(-pow(sat(uMisc.y), 1.85) * 3.2);
    float field = 0.0;
    vec2 flow = vec2(0.0);
    int count = int(uMisc.z);

    for (int i = 0; i < 32; i++) {
        if (i >= count) {
            break;
        }
        vec4 well = uWells[i];
        float mass = well.w;
        if (mass <= 0.001) {
            continue;
        }
        vec2 delta = pixel - well.xy;
        float radius = max(well.z, 1.0);
        float dist = length(delta);
        vec2 dir = dist > 0.001 ? delta / dist : vec2(0.0, 1.0);
        float shapedMass = mass * reveal;
        float falloff = exp(-pow(dist / radius, 2.0) * 1.18) * shapedMass;
        float core = exp(-pow(dist / max(radius * 0.52, 1.0), 2.0) * 1.95) * shapedMass;
        float tidal = falloff * (11.0 + radius * 0.060);
        warped -= dir * tidal;
        warped -= dir * core * (4.0 + radius * 0.018);
        flow += dir * falloff;
        field += falloff + core * 0.45;
    }

    vec2 cursorDelta = pixel - uCursor;
    float cursorDist = length(cursorDelta);
    float cursorHot = exp(-pow(cursorDist / 210.0, 2.0)) * reveal * 0.16;
    warped -= (cursorDist > 0.001 ? cursorDelta / cursorDist : vec2(0.0, 1.0)) * cursorHot * 2.4;

    float minor = lineGrid(warped, 50.0, 0.66 + sat(field) * 0.54);
    float major = lineGrid(warped, 250.0, 0.96 + sat(field) * 0.72);
    float fine = lineGrid(warped + flow * 1.6, 25.0, 0.26) * 0.10;
    float grid = max(minor * 0.62 + major * 0.74, fine);
    vec3 base = mix(uAccentBottom.rgb, uAccentTop.rgb, sat(0.43 + pixel.y / max(uResolution.y, 1.0) * 0.18));
    vec3 cold = vec3(0.34, 0.78, 1.0);
    vec3 color = mix(cold, base, 0.62);
    float alpha = grid * (0.12 + sat(field) * 0.24) + cursorHot * minor * 0.010;
    color += base * grid * sat(field) * 0.090;
    color += vec3(1.0) * major * sat(field) * 0.026;
    fragColor = vec4(clamp(color, 0.0, 1.0), sat(alpha) * reveal);
}
