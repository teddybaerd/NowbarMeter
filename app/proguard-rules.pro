# Add project specific ProGuard rules here.
# You can use the generated configuration data with the -printconfiguration directive.

# Keep Kotlin metadata (essential for Kotlin reflection and some libraries)
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,Signature

# Keep Parcelable CREATOR fields (often needed if not fully handled by plugin)
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable members if used
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Samsung Notification semFlags field for reflection
-keepclassmembers class android.app.Notification {
    public int semFlags;
}

# Third-party library specific rules
-dontwarn androidx.window.extensions.**
-dontwarn androidx.window.sidecar.**
