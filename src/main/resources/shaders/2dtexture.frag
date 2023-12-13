#version 330 core

uniform vec4 Color;
uniform sampler2D textureSampler;

in vec2 UV;
// in vec3 fragmentColor;
out vec4 color;

void main() {
    color = texture(textureSampler, UV) * Color;
}
