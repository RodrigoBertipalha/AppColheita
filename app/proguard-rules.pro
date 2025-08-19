-keepattributes *Annotation*
-keepattributes Signature

# Keep Apache POI classes
-keep class org.apache.poi.** { *; }
-keep class org.openxmlformats.** { *; }

# R8 full mode strips generic signatures from return types
-keepattributes Signature

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# XML related
-dontwarn org.xmlpull.v1.**
-dontwarn javax.xml.**
