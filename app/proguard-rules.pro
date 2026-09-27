-keep class com.noteremind.app.data.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
