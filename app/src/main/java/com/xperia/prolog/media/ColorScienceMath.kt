package com.xperia.prolog.media

/**
 * Mathematical transformations for industry standard Log curves.
 * Normally, these are applied via 3D LUTs in an OpenGL fragment shader when intercepting 
 * the raw YUV_420_888 camera stream before it hits the MediaCodec surface.
 */
object ColorScienceMath {

    /**
     * S-Log3 Mathematical Transfer Function
     * As defined by Sony: 
     * https://pro.sony/s3/2019/04/18151528/S-Log_user_en_v2.0.pdf
     * 
     * Maps linear sensor data (0.0 to 1.0) into S-Log3 curve.
     */
    fun linearToSLog3(linearValue: Float): Float {
        val x = linearValue.coerceIn(0f, 1f)
        return if (x < 0.01125000f) {
            (x * (171.2102946929f - 95.0f) / 0.01125000f + 95.0f) / 1023.0f
        } else {
            (420.0f + kotlin.math.log10((x + 0.01f) / (0.18f + 0.01f)) * 261.5f) / 1023.0f
        }
    }

    /**
     * Cineon Log Mathematical Transfer Function
     * As defined by Kodak (10-bit code values 0-1023)
     * Reference White: 685 (90% reflectance)
     * Reference Black: 95 (1% reflectance)
     * 
     * Formula: Code Value = (log10(Exposure) * 300) + 685
     * (Assuming Exposure is mapped around 0.18 mid-gray)
     */
    fun linearToCineonLog(linearValue: Float): Float {
        val x = linearValue.coerceIn(0.0001f, 1f) // Avoid log(0)
        // Transform based on standard Cineon 10-bit printing density curve
        val logDensity = kotlin.math.log10(x * (0.90f / 0.18f)) 
        val codeValue = (logDensity * 300f) + 685f
        return (codeValue / 1023f).coerceIn(0f, 1f)
    }
}

/**
 * GLSL Fragment Shader snippets to inject these math curves into the GPU pipeline
 * for real-time 4K60 encoding without dropping frames.
 */
object ColorScienceShaders {
    
    val SLOG3_FRAGMENT_SHADER = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform samplerExternalOES sTexture;

        float linearToSLog3(float x) {
            if (x < 0.01125000) {
                return (x * (171.2102946929 - 95.0) / 0.01125000 + 95.0) / 1023.0;
            } else {
                // Approximate log10 using log / log(10)
                return (420.0 + (log((x + 0.01) / 0.19) / 2.30258509299) * 261.5) / 1023.0;
            }
        }

        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            
            // Assume input surface is linear (or reverse gamma if it's already Rec709)
            // Apply S-Log3 curve per channel
            color.r = linearToSLog3(color.r);
            color.g = linearToSLog3(color.g);
            color.b = linearToSLog3(color.b);
            
            gl_FragColor = color;
        }
    """.trimIndent()

    val CINEON_FRAGMENT_SHADER = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextureCoord;
        uniform samplerExternalOES sTexture;

        float linearToCineon(float x) {
            float safeX = max(x, 0.0001);
            float logDensity = (log(safeX * 5.0) / 2.30258509299); 
            float codeValue = (logDensity * 300.0) + 685.0;
            return clamp(codeValue / 1023.0, 0.0, 1.0);
        }

        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            
            // Apply Cineon curve
            color.r = linearToCineon(color.r);
            color.g = linearToCineon(color.g);
            color.b = linearToCineon(color.b);
            
            gl_FragColor = color;
        }
    """.trimIndent()
}
