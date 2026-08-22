#version 150

uniform sampler2D DiffuseSampler;
uniform float Radius;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = vec4(0.0);

    float r = Radius * 0.01;

    color += texture(DiffuseSampler, texCoord) * 0.4;
    color += texture(DiffuseSampler, texCoord + vec2(r, 0.0)) * 0.15;
    color += texture(DiffuseSampler, texCoord - vec2(r, 0.0)) * 0.15;
    color += texture(DiffuseSampler, texCoord + vec2(0.0, r)) * 0.15;
    color += texture(DiffuseSampler, texCoord - vec2(0.0, r)) * 0.15;

    fragColor = color;
}