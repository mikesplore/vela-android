# ──── Retrofit ───────────────────────────────────────────────────────────────
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ──── OkHttp ─────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ──── Moshi ──────────────────────────────────────────────────────────────────
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# ──── Hilt ───────────────────────────────────────────────────────────────────
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ──── Room ───────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ──── Kotlin serialization ───────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ──── Your data/domain models (adjust package if you rename) ─────────────────
-keep class com.template.app.domain.model.** { *; }
-keep class com.template.app.core.data.remote.dto.** { *; }
-keep class com.template.app.core.data.local.entities.** { *; }
