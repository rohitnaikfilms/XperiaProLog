# ProGuard / R8 Rules for Xperia ProLog
# ========================================

# Keep the application entry point
-keep class com.xperia.prolog.MainActivity { *; }

# Camera2 API - reflection used internally
-keep class android.hardware.camera2.** { *; }

# MediaCodec / MediaMuxer - native JNI
-keep class android.media.MediaCodec { *; }
-keep class android.media.MediaMuxer { *; }
-keep class android.media.MediaFormat { *; }

# OpenGL ES - native
-keep class android.opengl.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Jetpack Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep data classes used by StateFlow
-keep class com.xperia.prolog.ui.ExposureState { *; }
-keep class com.xperia.prolog.camera.LensInfo { *; }
-keep class com.xperia.prolog.media.EncoderConfig { *; }
-keep class com.xperia.prolog.media.ColorProfile { *; }

# EGL extensions
-keep class android.opengl.EGLExt { *; }
-keep class android.opengl.EGL14 { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
