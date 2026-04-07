#ifdef GL_ES
precision highp float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform float u_pixelSize;

void main() {
    // Snap to game-pixel grid
    vec2 p = floor(gl_FragCoord.xy / u_pixelSize);

    float arcX = v_texCoords.x;
    float arcY = (v_texCoords.y - 0.5) * 2.0; // -1 to 1

    // Taper: converge to a point at each edge (thin at ends, wider in middle)
    float taper = arcX * (1.0 - arcX) * 4.0; // peaks at 1.0 in center, 0 at edges
    taper = taper * taper; // sharpen the falloff toward edges

    // Jagged lightning path with time animation
    float phase = u_time * 12.0;
    float path = 0.0;
    path += sin(arcX * 15.0 + phase) * 0.15;
    path += sin(arcX * 30.0 - phase * 1.7) * 0.08;
    path += sin(arcX * 55.0 + phase * 2.3) * 0.04;

    // Scale path amplitude by taper (waves flatten at edges)
    path *= taper;

    // Secondary branch
    float branch = 0.0;
    branch += sin(arcX * 20.0 - phase * 0.8 + 3.14) * 0.12;
    branch += sin(arcX * 45.0 + phase * 1.5) * 0.05;
    branch *= taper;

    // Distance from arcs
    float distMain = abs(arcY - path);
    float distBranch = abs(arcY - branch);
    float dist = min(distMain, distBranch);

    // Pixel-noise for organic jitter
    vec2 np = fract(p / 97.0) * 97.0;
    float noise = fract(sin(np.x * 12.9898 + np.y * 78.233 + u_time * 5.0) * 43758.5453);

    // Width of the arc — thin overall, thinnest at edges
    float width = 0.06 + noise * 0.03;
    width *= max(taper, 0.15); // minimum width to keep visible

    // Intensity based on distance from arc
    float intensity = 0.0;
    if (dist < width) {
        intensity = 1.0;
    } else if (dist < width * 2.5) {
        intensity = 0.5;
    } else if (dist < width * 4.0) {
        intensity = 0.2;
    }

    // Hard discard — no semi-transparent background
    if (intensity < 0.01) discard;

    // Color: white core → cyan → blue
    vec3 color;
    if (intensity > 0.8) {
        color = vec3(1.0, 1.0, 1.0);
    } else if (intensity > 0.4) {
        color = vec3(0.3, 0.8, 1.0);
    } else {
        color = vec3(0.1, 0.3, 1.0);
    }

    gl_FragColor = vec4(color, 1.0) * v_color;
}
