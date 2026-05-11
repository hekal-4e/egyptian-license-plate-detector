# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# TFLite
-keep class org.tensorflow.lite.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Supabase / Ktor Serialization
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-keep,allowobfuscation,allowshrinking class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keep,allowobfuscation,allowshrinking class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keep,allowobfuscation,allowshrinking class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# ZXing
-keep class com.google.zxing.** { *; }

# Ktor missing classes on Android
-dontwarn java.lang.management.**
-dontwarn javax.management.**

# TFLite GPU
-dontwarn org.tensorflow.lite.gpu.**
-keep class org.tensorflow.lite.gpu.** { *; }