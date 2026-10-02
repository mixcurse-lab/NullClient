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
float noise(vec2 p){
    vec2 i=floor(p), f=fract(p);
    float a=hash(i), b=hash(i+vec2(1.0,0.0)), c=hash(i+vec2(0.0,1.0)), d=hash(i+vec2(1.0,1.0));
    vec2 u=f*f*(3.0-2.0*f);
    return mix(a,b,u.x) + (c-a)*u.y*(1.0-u.x) + (d-b)*u.x*u.y;
}
float fbm(vec2 p){
    float total=0.0, amp=0.5;
    for(int i=0;i<5;i++){ total += noise(p)*amp; p *= 2.02; amp *= 0.55; }
    return total;
}
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
    vec3 p = rayW;
    float t = Time * max(Speed,0.1);

    vec2 cuv = p.xz * max(Scale,0.1) + vec2(t*0.15, t*0.08);
    float n = fbm(cuv);
    float inten = max(Intensity, 0.001);
    n = smoothstep(0.35, 0.35 + (0.5/inten), n);

    float heightMask = smoothstep(-0.1, 0.6, rayW.y);
    vec3 skyBase = mix(Color*0.5, Color*1.1, uv.y);
    vec3 col = mix(skyBase, vec3(1.0), n * heightMask);
    fragColor = vec4(col, 1.0);
}
