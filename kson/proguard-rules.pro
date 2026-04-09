# Kson Library ProGuard Rules

# Keep Kson public API
-keep public class com.nidoham.kson.** {
    public protected *;
}

# Keep annotations
-keepattributes *Annotation*
-keep @interface com.nidoham.kson.annotation.*

# Keep SerializedName alternate values
-keepclassmembers class * {
    @com.nidoham.kson.annotation.SerializedName <fields>;
}

# Keep classes with @JsonAdapter
-keep @com.nidoham.kson.annotation.JsonAdapter class * { *; }

# Keep Enum values
-keepclassmembers enum * {
    **[] values();
    public String valueOf(java.lang.String);
}

# Keep Sealed class subclasses
-keepclassmembers class * implements com.nidoham.kson.core.JsonElement { *; }

# Keep TypeAdapter implementations
-keep class * implements com.nidoham.kson.adapter.TypeAdapter { *; }
-keep class * implements com.nidoham.kson.adapter.TypeAdapterFactory { *; }

# Keep data classes used with Kson
-keepclassmembers class * {
    <init>(...);
}

# Kotlin Reflection
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep default constructors for serialization
-keepclassmembers class * {
    public <init>(...);
}

# Suppress warnings
-dontwarn kotlin.reflect.jvm.internal.**
-dontwarn kotlinx.coroutines.**