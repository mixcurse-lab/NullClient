float ui_coverage(float dist) {
    float width = max(fwidth(dist), 0.0001);
    return clamp(0.5 - dist / width, 0.0, 1.0);
}

float ui_coverageSoft(float dist, float softness) {
    float width = max(fwidth(dist), max(softness, 0.0001));
    return clamp(0.5 - dist / width, 0.0, 1.0);
}

float ui_screenPxRange(vec2 texCoord, vec2 atlasSize, float pxRange) {
    vec2 unitRange = vec2(pxRange) / atlasSize;
    vec2 screenTexSize = vec2(1.0) / max(fwidth(texCoord), vec2(0.0000001));
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}
