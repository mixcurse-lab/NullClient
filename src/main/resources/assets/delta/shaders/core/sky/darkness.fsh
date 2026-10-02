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
    float t = Time * max(Speed,0.1) * 0.5;
    float density=0.0;
    vec3 shift=vec3(t*1.5,t*1.0,t*0.7);
    vec3 coord=p+shift;
    float weight=1.0, totalWeight=0.0;
    for(int n=0;n<MAX_ITER;n++){
        float wave=sin(coord.x+sin(coord.y+t))*cos(coord.y+cos(coord.z-t))*sin(coord.z+t);
        density+=wave*weight; totalWeight+=weight;
        coord=coord*1.8+vec3(10.0,20.0,30.0); weight*=0.5;
    }
    density=(density/totalWeight)*0.5+0.5;
    float inten = max(Intensity,0.001);
    density=pow(density, max(0.5, 3.0 - (inten*40.0)));
    density=smoothstep(0.2,0.8,density);
    vec3 col=mix(vec3(0.0), Color*0.4, density);
    float highlight=pow(density,2.5);
    col+=Color*highlight*1.2;
    fragColor = vec4(col,1.0);
}
