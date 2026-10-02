#version 330 core
in vec2 uv;
out vec4 fragColor;
uniform sampler2D ScreenTexture;
uniform sampler2D DepthTexture;
layout(std140) uniform SkyUniforms {
    vec2 Resolution;
    float Time;
    vec3 Color;
    float Alpha;
    float Speed;
    float Scale;
    float Intensity;
    vec2 CameraDir;
    float Fov;
};
float hash(vec2 p){ p = fract(p*vec2(123.34,456.21)); p += dot(p,p+45.32); return fract(p.x*p.y); }
void main(){
    float depth = texture(DepthTexture, uv).r;
    float skyMask = 1.0 - step(0.00001, depth);
    vec3 scene = texture(ScreenTexture, uv).rgb;
    if(skyMask < 0.5){ fragColor = vec4(scene,1.0); return; }

    float aspect = Resolution.x / max(Resolution.y,1.0);
    vec2 sp = uv * vec2(aspect,1.0) * max(Scale,1.0) * 30.0;
    vec2 g = floor(sp);
    vec2 f = fract(sp);

    float t = Time * max(Speed,0.1);
    float colSeed = hash(vec2(g.x, 0.0));
    float fallSpeed = 4.0 + colSeed * 6.0;
    float y = g.y + t * fallSpeed;

    float trail = fract(y);
    float head = smoothstep(1.0, 0.0, trail);
    float lit = step(0.5, hash(vec2(g.x, floor(y) + 0.5)));
    float bright = head * lit;

    float glyph = step(0.5, hash(f + g*13.0 + floor(y)));
    float mask = glyph * bright;

    float inten = max(Intensity, 0.001);
    vec3 col = Color * mask * (0.6 + inten);
    col += vec3(1.0) * mask * head * head * 0.5;
    fragColor = vec4(col, 1.0);
}
