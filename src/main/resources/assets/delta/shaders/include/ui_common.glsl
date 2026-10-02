const float UI_SIZE_SCALE = 8.0;
const float UI_RADIUS_SCALE = 16.0;
const float UI_MSDF_HOLE_BIAS = 0.0625;

float ui_median3(vec3 v) {
    return max(min(v.r, v.g), min(max(v.r, v.g), v.b));
}

vec2 ui_unpackU8Pair(int encoded) {
    return vec2(float(encoded & 255), float((encoded >> 8) & 255)) / 255.0;
}

vec4 ui_unpackColor(int packedRg, int packedBa) {
    return vec4(ui_unpackU8Pair(packedRg), ui_unpackU8Pair(packedBa));
}

vec3 ui_unpackZ(float encoded) {
    float units = floor(encoded / 256.0);
    float alpha = encoded - units * 256.0;
    float mode = step(4096.0, units);
    units -= mode * 4096.0;
    return vec3(mode, units / UI_RADIUS_SCALE, alpha / 255.0);
}

vec2 ui_unpackDual12Raw(float encoded) {
    float hi = floor(encoded / 4096.0);
    float lo = encoded - hi * 4096.0;
    return vec2(hi, lo);
}

vec2 ui_unpackDual12(float encoded) {
    return ui_unpackDual12Raw(encoded) / UI_RADIUS_SCALE;
}

float ui_selectRadius(vec2 point, vec4 radii) {
    if (point.x >= 0.0) {
        return point.y < 0.0 ? radii.y : radii.z;
    }
    return point.y < 0.0 ? radii.x : radii.w;
}

vec4 ui_fitRadii(vec4 radii, vec2 size) {
    radii = max(radii, 0.0);
    float epsilon = 0.0001;
    float scale = min(1.0, min(
        min(size.x / max(radii.x + radii.y, epsilon), size.x / max(radii.w + radii.z, epsilon)),
        min(size.y / max(radii.x + radii.w, epsilon), size.y / max(radii.y + radii.z, epsilon))
    ));
    return radii * scale;
}

float ui_roundedBoxSdf(vec2 point, vec2 halfSize, vec4 radii) {
    float radius = ui_selectRadius(point, radii);
    vec2 q = abs(point) - halfSize + vec2(radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

float ui_roundedBoxSdfUniform(vec2 point, vec2 halfSize, float radius) {
    vec2 q = abs(point) - halfSize + vec2(radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

float ui_gaussFalloff(float dist, float radius) {
    float x = max(dist, 0.0) / max(radius, 0.0001);
    return exp(-4.5 * x * x);
}

vec4 ui_premultiply(vec4 color) {
    return vec4(color.rgb * color.a, color.a);
}

vec4 ui_compositePremul(vec4 back, vec4 front) {
    return front + back * (1.0 - front.a);
}

vec4 ui_unpremultiply(vec4 premul) {
    return vec4(premul.rgb / max(premul.a, 0.0001), premul.a);
}

float ui_dither(vec2 fragPos) {
    return (fract(sin(dot(fragPos, vec2(12.9898, 78.233))) * 43758.5453) - 0.5) / 255.0;
}
