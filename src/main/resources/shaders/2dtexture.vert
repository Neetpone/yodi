#version 330 core

uniform vec4 Color;
uniform mat4 ProjectionMatrix;

layout(location = 0) in vec2 vertexPosition_screenSpace;
layout(location = 1) in vec2 vertexUV;

// out vec3 fragmentColor;
out vec2 UV;

void main() {
    gl_Position = ProjectionMatrix * vec4(vertexPosition_screenSpace, 0, 1);
    UV = vertexUV;
}
