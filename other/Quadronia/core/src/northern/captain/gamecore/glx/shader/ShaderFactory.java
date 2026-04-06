package northern.captain.gamecore.glx.shader;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import northern.captain.gamecore.glx.tools.MySpriteBatch;
import northern.captain.tools.Log;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 28.04.13
 * Time: 14:37
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class ShaderFactory
{
    public static ShaderFactory singleton = new ShaderFactory();

    public static ShaderFactory instance()
    {
        return singleton;
    }

    private static class ShaderData
    {
        String vertexCode;
        String fragmentCode;

        ShaderData(String vertexCode, String fragmentCode)
        {
            this.vertexCode = vertexCode;
            this.fragmentCode = fragmentCode;
        }
    }

    private static final ShaderData sharpenData =
            new ShaderData(
                 "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                 "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

                 "uniform float imageWidthFactor;\n" +
                 "uniform float imageHeightFactor;\n" +
                 "uniform float sharpness;\n" +
                 "uniform mat4  u_projTrans;\n" +
                 "uniform vec4 "+ ShaderProgram.COLOR_ATTRIBUTE + ";\n" +

                 "varying vec2 textureCoordinate;\n" +
                 "varying vec2 leftTextureCoordinate;\n" +
                 "varying vec2 rightTextureCoordinate;\n" +
                 "varying vec2 topTextureCoordinate;\n" +
                 "varying vec2 bottomTextureCoordinate;\n" +

                 "varying float centerMultiplier;\n" +
                 "varying float edgeMultiplier;\n" +

                 "varying vec4 v_color;\n" +

                 "void main()\n" +
                 "{\n" +
                 "    gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +

                 "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
                 "    textureCoordinate = "+ ShaderProgram.TEXCOORD_ATTRIBUTE +"0;\n" +
                 "    mediump vec2 widthStep = vec2(imageWidthFactor, 0.0);\n" +
                 "    mediump vec2 heightStep = vec2(0.0, imageHeightFactor);\n" +

                 "    leftTextureCoordinate = "+ ShaderProgram.TEXCOORD_ATTRIBUTE + "0.xy - widthStep;\n" +
                 "    rightTextureCoordinate = "+ ShaderProgram.TEXCOORD_ATTRIBUTE +"0.xy + widthStep;\n" +
                 "    topTextureCoordinate = "+ ShaderProgram.TEXCOORD_ATTRIBUTE + "0.xy + heightStep;\n" +
                 "    bottomTextureCoordinate = "+ ShaderProgram.TEXCOORD_ATTRIBUTE + "0.xy - heightStep;\n" +

                 "    centerMultiplier = 1.0 + 4.0 * sharpness;\n" +
                 "    edgeMultiplier = sharpness;\n" +
                 "}\n",

                    //Fragment sharpen shader
                 "precision highp float;\n" +

                 "varying highp vec2 textureCoordinate;\n" +
                 "varying highp vec2 leftTextureCoordinate;\n" +
                 "varying highp vec2 rightTextureCoordinate;\n" +
                 "varying highp vec2 topTextureCoordinate;\n" +
                 "varying highp vec2 bottomTextureCoordinate;\n" +

                 "varying highp float centerMultiplier;\n" +
                 "varying highp float edgeMultiplier;\n" +
                 "varying vec4 v_color;\n" +
                 "uniform sampler2D u_texture;\n" +

                 "void main()\n" +
                 "{\n" +
                 "    mediump vec3 textureColor = texture2D(u_texture, textureCoordinate).rgb;\n" +
                 "    mediump vec3 leftTextureColor = texture2D(u_texture, leftTextureCoordinate).rgb;\n" +
                 "    mediump vec3 rightTextureColor = texture2D(u_texture, rightTextureCoordinate).rgb;\n" +
                 "    mediump vec3 topTextureColor = texture2D(u_texture, topTextureCoordinate).rgb;\n" +
                 "    mediump vec3 bottomTextureColor = texture2D(u_texture, bottomTextureCoordinate).rgb;\n" +

                 "    gl_FragColor = vec4((textureColor * centerMultiplier - (leftTextureColor * edgeMultiplier "+
                         "+ rightTextureColor * edgeMultiplier + topTextureColor * edgeMultiplier "+
                         "+ bottomTextureColor * edgeMultiplier)), texture2D(u_texture, bottomTextureCoordinate).w);\n" +
                 "}\n"
               
            );

    public CustomShader createSharpenShader()
    {
        CustomShader shader = new CustomShader(sharpenData.vertexCode, sharpenData.fragmentCode)
        {
            @Override
            public void applyVariables(MySpriteBatch batch)
            {
                setUniformf("imageWidthFactor", 1.0f / batch.currentTexture.getWidth());
                setUniformf("imageHeightFactor", 1.0f / batch.currentTexture.getHeight());
                setUniformf("sharpness", 0.1f);
            }
        };
        if (shader.isCompiled() == false) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
        return shader;
    }

    public CustomShader createStandardShader()
    {
        String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "varying vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n";
        String fragmentShader = "#ifdef GL_ES\n" //
                + "#define LOWP lowp\n" //
                + "precision mediump float;\n" //
                + "#else\n" //
                + "#define LOWP \n" //
                + "#endif\n" //
                + "varying LOWP vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "uniform sampler2D u_texture;\n" //
                + "void main()\n"//
                + "{\n" //
                + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" //
                + "}";

        CustomShader shader = new CustomShader(vertexShader, fragmentShader)
        {
            @Override
            public void applyVariables(MySpriteBatch spriteBatch)
            {
            }
        };
        if (shader.isCompiled() == false) throw new IllegalArgumentException("couldn't compile shader: " + shader.getLog());
        return shader;
    }

    private static final ShaderData waveData =
            new ShaderData(
                  "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "uniform mat4 u_projTrans;\n" //
                + "varying vec4 v_color;\n" //
                + "varying vec2 v_texCoords;\n" //
                + "\n" //
                + "void main()\n" //
                + "{\n" //
                + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                + "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
                + "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                + "}\n",

                    "#ifdef GL_ES\n" +
                            "#define LOWP lowp\n" +
                            "precision mediump float;\n" +
                            "#else\n" +
                            "#define LOWP\n" +
                            "#endif\n" +
                            "\n" +
                            "uniform float time;\n" +
                            "uniform vec2 resolution;\n" +
                            "uniform vec2 startposition;\n" +
                            "uniform sampler2D u_texture;\n" +
                            "\n" +
                            "varying LOWP vec4 v_color;\n" +
                            "varying vec2 v_texCoords;\n" +
                            "\n" +
                            "vec2 position;\n" +
                            "\n" +
                            "vec3 ball(vec3 colour, float sizec, float xc, float yc)\n" +
                            "{\n" +
                            "    float dist = distance(position, vec2(xc, yc));\n" +
                            "\treturn colour * (sizec / dist);\n" +
                            "}\n" +
                            "\n" +
                            "float clampfactor = 0.85;\n" +
                            "float fadefactor = 0.10;\n" +
                            "float timefactor = 9.0;\n" +
                            "float scalefactor = 30.0;\n" +
                            "\n" +
                            "vec4 wavecentric(vec3 color, float xc, float yc, float tim)\n" +
                            "{\n" +
                            "\tfloat realdist = distance(position, vec2(xc, yc));\n" +
                            "\tfloat sine = ((position.y - yc) / realdist + 1.0)/2.0;\n" +
                            "\trealdist *= 2.0;\n" +
                            "\tfloat dist = clamp(realdist, 0.0, clampfactor);\n" +
                            "\tfloat wave = clamp(((sin((dist + tim/timefactor)*scalefactor) + 1.0)/2.0)*dist + 0.0, 0.0, 1.0);\n" +
                            "    float edgefactor = 1.0 - clamp((realdist - clampfactor)/fadefactor, 0.0, 1.0);\n" +
                            "    float wf = wave * edgefactor;\n" +
                            "\treturn vec4( color.r * sine, color.g * (1.0 - sine), color.b, 0.8 ) * wf;\n" +
                            "}\n" +
                            "\n" +
                            "LOWP vec3 red = vec3(1, 1, 1);\n" +
                            "LOWP vec3 green = vec3(1, 2, 1);\n" +
                            "LOWP vec3 blue = vec3(1.4, 1.4, 0.0);\n" +
                            "void main( void )\n" +
                            "{\n" +
                            "\tfloat ratio = resolution.x / resolution.y;\n" +
                            "\tposition = ( ( gl_FragCoord.xy - startposition ) / resolution.xy );\n" +
                            "\tposition.y = position.y / ratio + 0.0;\n" +
                            "\n" +
                            "\tfloat zoom = clamp((sin(time) + 1.0)/2.0 + 0.2, 0.7, 1.0);\n" +
                            "\tfloat ballzoom = 2.6 / zoom;\n" +
                            "\n" +
                            "\tvec4 wavecolor = wavecentric(blue, 0.5, 0.5, time);" +
                            "\nLOWP vec3 color = wavecolor.rgb;\n" +
                            "\tLOWP vec3 ball1 = ball(red, 0.16, -sin(time*-2.0) / ballzoom + 0.5, -cos(time*-2.0) / ballzoom + 0.5) + 0.5;\n" +
                            "\tLOWP vec3 ball2 = ball(red, 0.16, -sin(time*2.2) / ballzoom + 0.5, -cos(time*2.2) / ballzoom + 0.5) + 0.7;\n" +
                            "\tgl_FragColor = vec4(color*ball1*ball2, wavecolor.a*ball1.r*ball2.r);\n" +
                            "}\n");

    private ShaderProgram createWaveShader()
    {
        //important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false;

        ShaderProgram shaderProgram = new ShaderProgram(waveData.vertexCode, waveData.fragmentCode);
        if (!shaderProgram.isCompiled())
        {
            Log.e("ncgame", "Shader ERROR: " + shaderProgram.getLog());
            System.exit(0);
        }
        if (shaderProgram.getLog().length()!=0)
        {
            Log.w("ncgame", "Shader WARNING: " + shaderProgram.getLog());
        }

        //setup uniforms for our shader
        shaderProgram.begin();
        shaderProgram.setUniformf("resolution", 100f, 100f);
        shaderProgram.setUniformf("startposition", 0f, 0f);
        shaderProgram.setUniformf("time", 0f);
        shaderProgram.end();

        return shaderProgram;
    }

    private ShaderProgram waveShader;

    public ShaderProgram getWaveShader()
    {
        if(waveShader == null)
        {
            waveShader = createWaveShader();
        }

        return waveShader;
    }

    private static final ShaderData blurData =
            new ShaderData(
            "attribute vec4 "+ShaderProgram.POSITION_ATTRIBUTE+";\n" +
                    "attribute vec4 "+ShaderProgram.COLOR_ATTRIBUTE+";\n" +
                    "attribute vec2 "+ShaderProgram.TEXCOORD_ATTRIBUTE+"0;\n" +

                    "uniform mat4 u_projTrans;\n" +
                    " \n" +
                    "varying vec4 vColor;\n" +
                    "varying vec2 vTexCoord;\n" +

                    "void main() {\n" +
                    "	vColor = "+ShaderProgram.COLOR_ATTRIBUTE+";\n" +
                    "	vTexCoord = "+ShaderProgram.TEXCOORD_ATTRIBUTE+"0;\n" +
                    "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                    "}",

            "#ifdef GL_ES\n" +
                    "#define LOWP lowp\n" +
                    "precision mediump float;\n" +
                    "#else\n" +
                    "#define LOWP \n" +
                    "#endif\n" +
                    "varying LOWP vec4 vColor;\n" +
                    "varying vec2 vTexCoord;\n" +
                    "\n" +
                    "uniform sampler2D u_texture;\n" +
                    "uniform float resolution;\n" +
                    "uniform float radius;\n" +
                    "uniform vec2 dir;\n" +
                    "\n" +
                    "void main() {\n" +
                    "	vec4 sum = vec4(0.0);\n" +
                    "	vec2 tc = vTexCoord;\n" +
                    "	float blur = radius/resolution; \n" +
                    "    \n" +
                    "    float hstep = dir.x;\n" +
                    "    float vstep = dir.y;\n" +
                    "    \n" +
                    "	sum += texture2D(u_texture, vec2(tc.x - 4.0*blur*hstep, tc.y - 4.0*blur*vstep)) * 0.05;\n" +
                    "	sum += texture2D(u_texture, vec2(tc.x - 3.0*blur*hstep, tc.y - 3.0*blur*vstep)) * 0.09;\n" +
                    "	sum += texture2D(u_texture, vec2(tc.x - 2.0*blur*hstep, tc.y - 2.0*blur*vstep)) * 0.12;\n" +
                    "	sum += texture2D(u_texture, vec2(tc.x - 1.0*blur*hstep, tc.y - 1.0*blur*vstep)) * 0.15;\n" +
                    "	\n" +
                    "	sum += texture2D(u_texture, vec2(tc.x, tc.y)) * 0.16;\n" +
                    "	\n" +
                    "	sum += texture2D(u_texture, vec2(tc.x + 1.0*blur*hstep, tc.y + 1.0*blur*vstep)) * 0.15;\n" +
                    "	sum += texture2D(u_texture, vec2(tc.x + 2.0*blur*hstep, tc.y + 2.0*blur*vstep)) * 0.12;\n" +
                    "	sum += texture2D(u_texture, vec2(tc.x + 3.0*blur*hstep, tc.y + 3.0*blur*vstep)) * 0.09;\n" +
                    "	sum += texture2D(u_texture, vec2(tc.x + 4.0*blur*hstep, tc.y + 4.0*blur*vstep)) * 0.05;\n" +
                    "\n" +
                    "	gl_FragColor = vColor * sum;//vec4(sum.rgb, 1.0);\n" +
                    "}");


    private ShaderProgram blurShader;

    public ShaderProgram getBlurShader()
    {
        if(blurShader == null)
        {
            blurShader = createBlurShader();
        }

        return blurShader;
    }

    private ShaderProgram createBlurShader()
    {
        //important since we aren't using some uniforms and attributes that SpriteBatch expects
        ShaderProgram.pedantic = false;

        ShaderProgram blurShader = new ShaderProgram(blurData.vertexCode, blurData.fragmentCode);
        if (!blurShader.isCompiled())
        {
            Log.e("ncgame", blurShader.getLog());
            System.exit(0);
        }
        if (blurShader.getLog().length()!=0)
        {
            Log.w("ncgame", blurShader.getLog());
        }

        //setup uniforms for our shader
        blurShader.begin();
        blurShader.setUniformf("dir", 0f, 0f);
        blurShader.setUniformf("resolution", 100);
        blurShader.setUniformf("radius", 1f);
        blurShader.end();

        return blurShader;
    }
}
