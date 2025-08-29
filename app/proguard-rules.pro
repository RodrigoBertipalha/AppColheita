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

# Ignore missing classes warnings - Apache POI desktop dependencies
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.swing.**
-dontwarn org.apache.batik.**
-dontwarn org.apache.pdfbox.**
-dontwarn org.apache.xml.security.**
-dontwarn org.apache.jcp.xml.dsig.**
-dontwarn org.bouncycastle.**
-dontwarn net.sf.saxon.**
-dontwarn org.ietf.jgss.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn org.osgi.framework.**
-dontwarn org.w3c.dom.events.**
-dontwarn org.w3c.dom.svg.**
-dontwarn org.w3c.dom.traversal.**
-dontwarn aQute.bnd.annotation.**
-dontwarn de.rototor.pdfbox.graphics2d.**
-dontwarn edu.umd.cs.findbugs.annotations.**

# Keep only what we actually use from Apache POI
-keep class org.apache.poi.xssf.usermodel.** { *; }
-keep class org.apache.poi.ss.usermodel.** { *; }
-keep class org.apache.poi.util.** { *; }

# Remove unused features that depend on AWT
-assumenosideeffects class org.apache.poi.** {
    *** drawImage(...);
    *** getImage(...);
    *** createGraphics(...);
}
