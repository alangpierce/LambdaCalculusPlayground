# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/apierce/Library/Android/sdk/tools/proguard/proguard-android.txt
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

# Never do obfuscation. I don't want to discourage reverse-engineering (and the code is on GitHub
# anyway), and the debugging/correctness headaches probably aren't worth the slightly smaller apk
# size.
-dontobfuscate
