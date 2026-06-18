# ── Compose ────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Stable <fields>;
}

# ── App data classes (Gson serialization) ─────────────────────────
-keep class com.example.beamsyncmobile.data.history.** { *; }
-keep class com.example.beamsyncmobile.ui.screens.downloads.ReceiveViewModelKt { *; }
-keep class com.example.beamsyncmobile.ui.screens.uploads.UploadViewModelKt { *; }
-keep class com.example.beamsyncmobile.ui.screens.scan.QrScannerViewModelKt { *; }

# ML Kit (reflection-based DI — entire library must be kept un-obfuscated)
-keep class com.google.mlkit.** { *; }
-keepclassmembers class com.google.mlkit.** {
    <init>();
}
-keep class com.google.android.gms.vision.** { *; }
-keepclassmembers class com.google.android.gms.vision.** {
    <init>();
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Coil
-dontwarn coil.**
-keep class coil.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
