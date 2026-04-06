#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 resolution;

vec2 position;

vec3 ball(vec3 colour, float sizec, float xc, float yc){
	return colour * (sizec / distance(position, vec2(xc, yc)));
}

vec3 grid(vec3 colour, float linesize, float xc, float yc){
	float xmod = mod(position.x, xc);
	float ymod = mod(position.y, yc);
	return xmod < linesize || ymod < linesize ? vec3(0) : colour;
}

vec3 circle(vec3 colour, float size, float linesize, float xc, float yc){
	float dist = distance(position, vec2(xc, yc));
	return colour * clamp(-(abs(dist - size)*linesize * 50.0) + 0.5, 0.1, 1.0);
}

vec3 red = vec3(2, 1, 1);
vec3 green = vec3(1, 2, 1);
vec3 blue = vec3(1, 1, 2);
void main( void ) 
{
	position = ( gl_FragCoord.xy / resolution.xy );
	position.y = position.y * resolution.y/resolution.x + 0.25;
	
	vec3 color = vec3(0.0);
	float ratio = resolution.x / resolution.y;
	float zoom = clamp(abs(pow(sin(time),2.0)), 0.2, 1.0);
	float ballzoom = 12.0 / zoom;
	color += circle(blue, 0.085 * zoom, 0.6, 0.5, 0.5);
	
	//color += grid(blue * 0.1, 0.001, 0.06, 0.06);
	//color *= 1.0 - distance(position, vec2(0.5, 0.5));
	color += ball(green, 0.01, sin(time*4.0) / ballzoom + 0.5, cos(time*4.0) / ballzoom + 0.5);
	color *= ball(red, 0.01, -sin(time*-8.0) / ballzoom + 0.5, -cos(time*-8.0) / ballzoom + 0.5) + 0.5;
	gl_FragColor = vec4(color , 1.0 );
}

/// Variant 2, improved

#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 resolution;

vec2 position;

vec3 ball(vec3 colour, float sizec, float xc, float yc){
	return colour * (sizec / distance(position, vec2(xc, yc)));
}

vec3 grid(vec3 colour, float linesize, float xc, float yc){
	float xmod = mod(position.x, xc);
	float ymod = mod(position.y, yc);
	return xmod < linesize || ymod < linesize ? vec3(0) : colour;
}

vec3 circle(vec3 colour, float size, float linesize, float xc, float yc){
	float dist = distance(position, vec2(xc, yc));
	return colour * clamp(-(abs(dist - size)*linesize * 50.0) + 0.5, 0.1, 1.0);
}

vec3 red = vec3(2, 1, 1);
vec3 green = vec3(1, 2, 1);
vec3 blue = vec3(1, 1, 2);
void main( void )
{
	float ratio = resolution.x / resolution.y;
	position = ( gl_FragCoord.xy / resolution.xy );
	position.y = position.y / ratio + 0.25;

	vec3 color = vec3(0.0);
	float zoom = clamp((sin(time) + 1.0)/4.0 + 0.4, 0.4, 1.0);
	float ballzoom = 12.0 / zoom;
	color += circle(blue, 0.085 * zoom, 0.8, 0.5, 0.5);
	color += circle(blue, 0.045 * zoom, 0.8, 0.5, 0.5);

	//color += grid(blue * 0.1, 0.001, 0.06, 0.06);
	//color *= 1.0 - distance(position, vec2(0.5, 0.5));
	//color += ball(green, 0.01, sin(time*4.0) / ballzoom + 0.5, cos(time*4.0) / ballzoom + 0.5);
	color *= ball(red, 0.01, -sin(time*-2.0) / ballzoom + 0.5, -cos(time*-2.0) / ballzoom + 0.5) + 0.5;
	color *= ball(red, 0.01, -sin(time*2.2) / ballzoom + 0.5, -cos(time*2.2) / ballzoom + 0.5) + 0.7;
	gl_FragColor = vec4(color , 1.0 );
}


//Moving Wave with balls
#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 resolution;
uniform vec2 startposition;

vec2 position;

vec3 ball(vec3 colour, float sizec, float xc, float yc)
{
    float dist = distance(position, vec2(xc, yc));
	return colour * (sizec / dist);
}

float clampfactor = 0.45;
float fadefactor = 0.1;
float timefactor = 20.0;
float scalefactor = 40.0;

vec3 wavecentric(vec3 color, float xc, float yc, float tim)
{
	float realdist = distance(position, vec2(xc, yc))*2.0;
	float dist = clamp(realdist, 0.0, clampfactor);
	float wave = clamp(((sin((pow(dist, 0.5)+ tim/timefactor)*scalefactor) + 1.0)/2.0)*pow(dist, 0.5) + 0.0, 0.0, 1.0);
    float edgefactor = 1.0 - pow(clamp((realdist - clampfactor)/fadefactor, 0.0, 1.0), 0.8);
	return color * wave * edgefactor;
}

vec3 red = vec3(2, 1, 1);
vec3 green = vec3(1, 2, 1);
vec3 blue = vec3(1, 1, 2);
void main( void )
{
	float ratio = resolution.x / resolution.y;
	position = ( ( gl_FragCoord.xy - startposition ) / resolution.xy );
	position.y = position.y / ratio + 0.25;

	vec3 color = vec3(0.0);
	float zoom = clamp((sin(time) + 1.0)/2.0 + 0.2, 0.4, 1.0);
	float ballzoom = 6.0 / zoom;

	color = wavecentric(red, 0.5, 0.5, time);

	color *= ball(blue, 0.03, -sin(time*-2.0) / ballzoom + 0.5, -cos(time*-2.0) / ballzoom + 0.5) + 0.5;
	color *= ball(blue, 0.03, -sin(time*2.2) / ballzoom + 0.5, -cos(time*2.2) / ballzoom + 0.5) + 0.7;
	gl_FragColor = vec4(color , 1.0 );
}

/// Adaptation
//Moving Wave with balls
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform float time;
uniform vec2 resolution;
uniform vec2 startposition;
uniform sampler2D u_texture;

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

vec2 position;

vec3 ball(vec3 colour, float sizec, float xc, float yc)
{
    float dist = distance(position, vec2(xc, yc));
	return colour * (sizec / dist);
}

float clampfactor = 0.45;
float fadefactor = 0.1;
float timefactor = 20.0;
float scalefactor = 40.0;

vec3 wavecentric(vec3 color, float xc, float yc, float tim)
{
	float realdist = distance(position, vec2(xc, yc))*2.0;
	float dist = clamp(realdist, 0.0, clampfactor);
	float wave = clamp(((sin((pow(dist, 0.5)+ tim/timefactor)*scalefactor) + 1.0)/2.0)*pow(dist, 0.5) + 0.0, 0.0, 1.0);
    float edgefactor = 1.0 - pow(clamp((realdist - clampfactor)/fadefactor, 0.0, 1.0), 0.8);
	return color * wave * edgefactor;
}

vec3 red = vec3(2, 1, 1);
vec3 green = vec3(1, 2, 1);
vec3 blue = vec3(1, 1, 2);
void main( void )
{
	float ratio = resolution.x / resolution.y;
	position = ( ( gl_FragCoord.xy - startposition ) / resolution.xy );
	position.y = position.y / ratio + 0.25;

	vec3 color = vec3(0.0);
	float zoom = clamp((sin(time) + 1.0)/2.0 + 0.2, 0.4, 1.0);
	float ballzoom = 6.0 / zoom;

	color = wavecentric(red, 0.5, 0.5, time);

	color *= ball(blue, 0.03, -sin(time*-2.0) / ballzoom + 0.5, -cos(time*-2.0) / ballzoom + 0.5) + 0.5;
	color *= ball(blue, 0.03, -sin(time*2.2) / ballzoom + 0.5, -cos(time*2.2) / ballzoom + 0.5) + 0.7;
	gl_FragColor = vec4(color , 1.0 );
}

//====== spiral
#ifdef GL_ES
precision mediump float;
#endif

uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;

float clampfactor = 0.88;
float fadefactor = 0.2;

#define PI 3.1415926535
void main( void ) {

	vec2 p = 2.0*( gl_FragCoord.xy / resolution.xy )-1.0;
	p.x *= resolution.x / resolution.y;
	vec3 col = vec3(0,0.6,1);

	float ang = atan(p.y,p.x);
	float dist = length(p);
	ang += 1.0/dist+time/2.0;
	ang = mod(ang*10.0, PI);
	if (abs(ang) < PI/2.0)
		col = vec3(1,0.7,0);
	float edgefactor = 1.0 - clamp((dist - clampfactor)/fadefactor, 0.0, 1.0);
	col *= dist * edgefactor;
	gl_FragColor = vec4(col, 1.0);
}

