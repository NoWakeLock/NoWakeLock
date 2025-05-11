# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keep class com.js.nowakelock.xposedhook.XposedModule

-repackageclasses
-allowaccessmodification
-overloadaggressively
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Fix for kotlinx.serialization issues with R8 full mode
# Prevents serializers from being stripped for @Serializable objects
#-if @kotlinx.serialization.Serializable class **
#-keep classmembers class <1> {
#    public static <1> INSTANCE;
#    kotlinx.serialization.KSerializer serializer(...);
#}
#https://github.com/Kotlin/kotlinx.serialization/issues/2861
-keep @kotlinx.serialization.Serializable class * {*;}