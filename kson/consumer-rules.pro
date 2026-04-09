# Consumer ProGuard Rules for Kson Library Users

# Keep Kson library classes
-keep class com.nidoham.kson.** { *; }

# Keep user's data classes that use Kson annotations
-keepclassmembers class * {
    @com.nidoham.kson.annotation.SerializedName <fields>;
    @com.nidoham.kson.annotation.Expose <fields>;
    @com.nidoham.kson.annotation.Transient <fields>;
    @com.nidoham.kson.annotation.JsonAdapter <fields>;
    @com.nidoham.kson.annotation.Since <fields>;
    @com.nidoham.kson.annotation.Until <fields>;
    @com.nidoham.kson.annotation.DefaultValue <fields>;
}

# Keep Enum classes
-keepclassmembers enum * {
    **[] values();
    **[] $VALUES;
    public String name();
    public String valueOf(java.lang.String);
}

# Keep Sealed classes and subclasses
-keep class * implements com.nidoham.kson.adapter.TypeAdapter { *; }
-keep class * implements com.nidoham.kson.adapter.TypeAdapterFactory { *; }