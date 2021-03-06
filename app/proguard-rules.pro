# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Thep\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontwarn android.support.**
-verbose

-dontwarn com.androidquery.**
-dontwarn com.actionbarsherlock.**
-dontwarn com.facebook.**
-dontwarn org.apache.**
-dontwarn com.stripe.android.**
-dontwarn org.mockito.**
-dontwarn sun.reflect.**
-dontwarn android.test.**
-dontwarn com.estimote.sdk.**
-dontwarn com.squareup.picasso.**
-dontwarn twitter4j.**
-dontwarn com.yetistep.**
-dontwarn com.squareup.timessquare.**
-dontwarn com.viewpagerindicator.**
-dontwarn com.astuetz.pagerslidingtabstrip.**
-dontwarn com.google.android.gms.**

-dontoptimize
-dontpreverify


-keepattributes *Annotation*
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

 -keepclassmembers public class * extends android.view.View {
  void set*(***);
  *** get*();
 }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
  public static <fields>;
}
-keep class com.wang.avi.** { *; }
-keep class com.wang.avi.indicators.** { *; }
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}