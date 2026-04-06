#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;    // tile index map (grayscale, value = index * 17)
uniform sampler2D u_palette;    // 16x26 palette texture
uniform float u_paletteRow;     // palette row (0-25)

void main() {
    vec4 texel = texture2D(u_texture, v_texCoords);

    // Index map stores index * 17 / 255, so multiply by 15 to get 0-15 range
    // Then normalize to 0.0-1.0 for the 16-wide palette lookup
    float index = texel.r * 15.0;
    float u = (index + 0.5) / 16.0;
    float v = (u_paletteRow + 0.5) / 26.0;

    vec4 color = texture2D(u_palette, vec2(u, v));

    // Index 0 = transparent (background)
    if (index < 0.5) discard;

    gl_FragColor = color * v_color;
}
