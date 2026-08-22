uniform sampler2D DiffuseSampler;
uniform vec2 Velocity;
uniform float Strength;

in vec2 texCoord;
out vec4 color;

void main() {
    vec4 col = vec4(0.0);

    float samples = 12.0;

    for (float i = 0.0; i < samples; i++) {
        float t = i / samples;
        vec2 offset = Velocity * Strength * (t - 0.5);
        col += texture(DiffuseSampler, texCoord + offset);
    }

    col /= samples;
    color = col;
}