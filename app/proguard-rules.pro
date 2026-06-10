# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information and original source file names for cleaner stack traces
-keepattributes SourceFile, LineNumberTable

# Moshi rules to prevent minification of JSON models and their generated adapters
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keep @interface com.squareup.moshi.Json
-keep @interface com.squareup.moshi.JsonClass
-keep class *JsonAdapter { public <init>(...); }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class * {
    *** <fields>;
    *** <init>(...);
    void set*(***);
    *** get*();
}

# Room Database structural integrity preservation
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Dao
-dontwarn androidx.room.Room

