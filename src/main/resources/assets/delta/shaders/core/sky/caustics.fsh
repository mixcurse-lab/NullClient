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
#define MAX_ITER 5
mat3 rotX(float a){float c=cos(a),s=sin(a);return mat3(1.0,0.0,0.0,0.0,c,s,0.0,-s,c);}
mat3 rotY(float a){float c=cos(a),s=sin(a);return mat3(c,0.0,s,0.0,1.0,0.0,-s,0.0,c);}
mat2 rot2(float a){float c=cos(a),s=sin(a);return mat2(c,-s,s,c);}
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
    vec3 p = rayW * max(Scale,1.0);
    float t = Time * max(Speed,0.1) * 0.6;

    vec2 q = p.xz * 3.0;
    float c = 0.0;
    for(int i=0;i<MAX_ITER;i++){
        float fi = float(i);
        vec2 qq = rot2(fi*0.8) * q * (1.0 + fi*0.15);
        c += abs(sin(qq.x + sin(qq.y + t)) * cos(qq.y - cos(qq.x + t)));
    }
    c /= float(MAX_ITER);
    float inten = max(Intensity, 0.001);
    c = pow(c, max(1.5, 4.0 - inten*20.0)) * (2.0 + inten*4.0);

    vec3 water = Color * 0.3;
    vec3 col = water + Color * c * 1.4;
    fragColor = vec4(col, 1.0);
}
