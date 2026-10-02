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
mat3 rotX(float a){float c=cos(a),s=sin(a);return mat3(1.0,0.0,0.0,0.0,c,s,0.0,-s,c);}
mat3 rotY(float a){float c=cos(a),s=sin(a);return mat3(c,0.0,s,0.0,1.0,0.0,-s,0.0,c);}
float hash(vec2 p){ p = fract(p*vec2(123.34,456.21)); p += dot(p,p+45.32); return fract(p.x*p.y); }
float hash3(vec3 p){ return hash(p.xy + hash(p.yz)*17.13); }
void main(){
    float depth = texture(DepthTexture, uv).r;
    float skyMask = 1.0 - step(0.00001, depth);
    vec3 scene = texture(ScreenTexture, uv).rgb;
    if(skyMask < 0.5){ fragColor = vec4(scene,1.0); return; }
    vec2 sp = uv*2.0-1.0;
    float aspect = Resolution.x / max(Resolution.y,1.0);
    float fovSafe = max(Fov, 10.0);
    float tanV = tan(radians(fovSafe)*0.5);
    vec3 rayV = normalize(vec3(sp.x*tanV*aspect, sp.y*tanV, 1.0));
    vec3 rayW = rotY(CameraDir.x) * rotX(CameraDir.y) * rayV;


    vec3 dir = rayW * max(Scale, 1.0);
    vec3 cell = floor(dir * 40.0);
    vec3 fcell = fract(dir * 40.0) - 0.5;

    float starChance = hash3(cell);
    float inten = max(Intensity, 0.001);
    float size = smoothstep(1.0 - inten*0.15, 1.0, starChance);
    float d = length(fcell);
    float glow = smoothstep(0.5, 0.0, d) * size;

    float t = Time * max(Speed, 0.1);
    float twinkle = 0.6 + 0.4 * sin(t*3.0 + starChance*40.0);

    vec3 base = mix(vec3(0.0,0.0,0.01), Color*0.05, uv.y);
    vec3 col = base + vec3(1.0) * glow * twinkle;
    fragColor = vec4(col, 1.0);
}
