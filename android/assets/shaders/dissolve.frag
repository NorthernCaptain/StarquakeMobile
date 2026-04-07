#ifdef GL_ES
precision highp float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_dissolve;  // 0 = fully visible, 1 = fully gone
uniform float u_pixelSize; // screen pixels per game pixel

void main() {
    vec4 texel = texture2D(u_texture, v_texCoords);
    if (texel.a < 0.5) discard;

    // Snap to game-pixel grid so dissolve blocks match the art's pixel size
    vec2 p = floor(gl_FragCoord.xy / u_pixelSize);
    p = fract(p / 97.0) * 97.0;
    float n = fract(sin(p.x * 12.9898 + p.y * 78.233) * 43758.5453);

    if (n < u_dissolve) discard;

    gl_FragColor = texel * v_color;
}
