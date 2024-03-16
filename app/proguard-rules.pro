# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn org.kxml2.**
-dontwarn org.xmlpull.v1.**
-dontwarn android.content.res.XmlResourceParser
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.javax.net.ssl.**
-dontwarn org.openjsse.net.ssl.**
-dontwarn javax.lang.model.element.**

-keep class org.kxml2.** { *; }
-keep class org.xmlpull.** { *; }
-keep class android.content.res.XmlResourceParser

######################################################################
######################## RETROFIT2 RULES #############################
######################################################################

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.**

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# keep everything in this package from being removed or renamed
-keep class com.bytecause.nautichart.domain.model.** { *; }

# keep everything in this package from being renamed only
-keepnames class com.bytecause.nautichart.domain.model.** { *; }

############################################################################################
##################### RULE ALLOWING TO LOAD DRAWABLES USING FIELD ##########################
############################################################################################

# keep the class and specified members from being removed or renamed
-keep class com.bytecause.nautichart.R$drawable { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.bytecause.nautichart.R$drawable { *; }

# keep the class and specified members from being renamed only
-keepnames class com.bytecause.nautichart.R$drawable { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.bytecause.nautichart.R$drawable { *; }

############################################################################
######################## PROTO DATASTORE RULES #############################
############################################################################

# keep the class and specified members from being removed or renamed
-keep class com.bytecause.nautichart.CustomPoiCategory { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.bytecause.nautichart.CustomPoiCategory { *; }

# keep the class and specified members from being renamed only
-keepnames class com.bytecause.nautichart.CustomPoiCategory { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.bytecause.nautichart.CustomPoiCategory { *; }

# keep the class and specified members from being removed or renamed
-keep class com.bytecause.nautichart.CustomPoiCategoryList { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.bytecause.nautichart.CustomPoiCategoryList { *; }

# keep the class and specified members from being renamed only
-keepnames class com.bytecause.nautichart.CustomPoiCategoryList { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.bytecause.nautichart.CustomPoiCategoryList { *; }

# keep the class and specified members from being removed or renamed
-keep class com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon { *; }

# keep the class and specified members from being renamed only
-keepnames class com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.bytecause.nautichart.RecentlyUsedPoiMarkerIcon { *; }

# keep the class and specified members from being removed or renamed
-keep class com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList { *; }

# keep the class and specified members from being renamed only
-keepnames class com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.bytecause.nautichart.RecentlyUsedPoiMarkerIconList { *; }

# keep the class and specified members from being removed or renamed
-keep class com.bytecause.nautichart.RecentlySearchedPlace { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.bytecause.nautichart.RecentlySearchedPlace { *; }

# keep the class and specified members from being renamed only
-keepnames class com.bytecause.nautichart.RecentlySearchedPlace { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.bytecause.nautichart.RecentlySearchedPlace { *; }

# keep the class and specified members from being removed or renamed
-keep class com.bytecause.nautichart.RecentlySearchedPlaceList { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.bytecause.nautichart.RecentlySearchedPlaceList { *; }

# keep the class and specified members from being renamed only
-keepnames class com.bytecause.nautichart.RecentlySearchedPlaceList { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.bytecause.nautichart.RecentlySearchedPlaceList { *; }

# keep the class and specified members from being removed or renamed
-keep class com.google.android.icing.IcingSearchEngineImpl { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.google.android.icing.IcingSearchEngineImpl { *; }

# keep the class and specified members from being renamed only
-keepnames class com.google.android.icing.IcingSearchEngineImpl { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.google.android.icing.IcingSearchEngineImpl { *; }






