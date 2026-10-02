#version 330 core

layout(location = 0) in vec3 Position;

void main() {
    gl_Position = vec4(Position.xy, 0.0, 1.0);
}
