# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# For reproducible builds
-optimizationpasses 5
-dontpreverify

-keep class com.js.nowakelock.xposedhook.XposedModule
-keep class com.js.nowakelock.xposedhook.ModernXposedModule { *; }
-keep class com.js.nowakelock.xposedhook.OldModernEntryAdapter { *; }
-keep class com.js.nowakelock.xposedhook.entry.** { *; }
-dontwarn io.github.libxposed.api.XposedModuleInterface$SystemServerLoadedParam
-keepclassmembers class com.js.nowakelock.xposedhook.ModernXposedModule {
    public <init>();
}
-keep class com.js.nowakelock.xposedhook.XposedHookInstallGuard
-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

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
