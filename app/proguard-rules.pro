# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradleold.
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

#打印混淆信息
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#-keepattributes SourceFile,LineNumberTable
#-keepattributes Exceptions, Signature, InnerClasses, EnclosingMethod
-keepclassmembers class androidx.fragment.app.Fragment { *; }
#保留di，reflect相关
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留banner类混淆
-keep class androidx.recyclerview.** {*;}
-keep class androidx.viewpager2.** {*;}

# 保留所有的Model类
-keep public class com.nova.model.** { *; }

#移动到跟目录/
-repackageclasses ''
-allowaccessmodification
-useuniqueclassmembernames
-keeppackagenames doNotKeepAThing

# ------------------------------------- 数字 DNA begin -------------------------------------
-keep class cn.shuzilm.core.Main {
    public *;
}
-keep class cn.shuzilm.core.Listener {
    public *;
}
-keepclasseswithmembernames class cn.shuzilm.core.Main {
    native <methods>;
}
-keep class cn.shuzilm.core.** {*;}

#混淆资源名
-adaptresourcefilenames **.propertie,**.version,**.gif,**.jpg
#-adaptresourcefilecontents **.properties,**.version,META-INF/MANIFEST.MF

# 为什么不混淆class_spec？
#-whyareyoukeeping class com.gmlive.common.dynamicdomain.DynamicDomain { *; }

#af混淆处理
-keep class com.appsflyer.** { *; }
-keep public class com.android.installreferrer.** { *; }

# -------------------------------------- anim --------------------------------------
-keep class org.cocos2dx.**{*;}
-dontwarn org.cocos2dx.**
-keep class com.opensource.svgaplayer.**{*;}
-dontwarn com.opensource.svgaplayer.**
-keep class eu.davidea.flexibleadapter.**{*;}
-dontwarn eu.davidea.flexibleadapter.**
# ------------------------------------------- Mars------------------------------------------
-keep class com.tencent.mars.xlog.** { *; }
-dontwarn com.tencent.mars.xlog.**