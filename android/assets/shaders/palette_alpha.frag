#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;    // sprite index map (grayscale, value = index * 17)
uniform sampler2D u_palette;    // 16x26 palette texture
uniform float u_paletteRow;     // palette row (0-25)

void main() {
    vec4 texel = texture2D(u_texture, v_texCoords);
    if (texel.a < 0.5) discard;

    float index = texel.r * 15.0;
    float u = (index + 0.5) / 16.0;
    float v = (u_paletteRow + 0.5) / 26.0;

    vec4 color = texture2D(u_palette, vec2(u, v));
    gl_FragColor = vec4(color.rgb, color.a * texel.a) * v_color;
}
