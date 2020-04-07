# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/jipengfei/android/android-studio/sdk/tools/proguard/proguard-android.txt
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

-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
-dontwarn cyee.**
-dontwarn com.cyee.**
-dontwarn com.gionee.appupgrade.**
-dontwarn com.gionee.dataghost.plugin.**
-keep class com.android.note.app.span.** { *;}
-keep class com.android.note.app.view.** { *;}
