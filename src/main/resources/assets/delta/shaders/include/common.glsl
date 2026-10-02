float rdist(vec2 pos, vec2 size, vec4 radius) {
    radius.xy = (pos.x > 0.0) ? radius.xy : radius.wz;
    radius.x  = (pos.y > 0.0) ? radius.x : radius.y;

    vec2 v = abs(pos) - size + radius.x;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - radius.x;
}

float ralpha(vec2 size, vec2 coord, vec4 radius, float smoothness) {
    vec2 center = size * 0.5;
    float dist = rdist(center - (coord * size), center - 1.0, radius);
    return 1.0 - smoothstep(1.0 - smoothness, 1.0, dist);
}

const vec2[4] RECT_VERTICES_COORDS = vec2[] (
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(1.0, 0.0)
);

vec2 rvertexcoord(int id) {
    return RECT_VERTICES_COORDS[id % 4];
}

const float DELTA_SIZE_SCALE = 8.0;
const float DELTA_RADIUS_SCALE = 16.0;
const float DELTA_DUAL12 = 4096.0;
const float DELTA_MODE_FLAG = 4096.0;
const float DELTA_Z_ALPHA_SCALE = 256.0;

vec2 deltaDecodeSize(ivec2 uv1) {
    return vec2(uv1) / DELTA_SIZE_SCALE;
}

vec4 deltaDecodeRadius(ivec2 uv2, float lineWidth) {
    float tl = float(uv2.x) / DELTA_RADIUS_SCALE;
    float tr = float(uv2.y) / DELTA_RADIUS_SCALE;
    float br = floor(lineWidth / DELTA_DUAL12) / DELTA_RADIUS_SCALE;
    float bl = mod(lineWidth, DELTA_DUAL12) / DELTA_RADIUS_SCALE;
    return vec4(tl, bl, br, tr);
}

vec2 deltaDecodeEffectZ(float z, out float effectAlpha, out float mode) {
    float units = floor(z / DELTA_Z_ALPHA_SCALE);
    effectAlpha = mod(z, DELTA_Z_ALPHA_SCALE) / 255.0;
    mode = (units >= DELTA_MODE_FLAG) ? 1.0 : 0.0;
    if (mode > 0.5) {
        units -= DELTA_MODE_FLAG;
    }
    return vec2(units / DELTA_RADIUS_SCALE, mode);
}