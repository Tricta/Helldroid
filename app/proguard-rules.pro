-keep, allowoptimization public class com.lib.hellcrypt.hellcrypt {
    public static void init();
}

-keep, allowoptimization public class com.lib.hellcrypt.Stub {
    public static final com.lib.hellcrypt.Stub instance;
    public java.lang.String hellYoki(java.lang.String);
}

-keep, allowoptimization public class com.lib.hellcrypt.hellDecrypt {
    public static java.lang.String hellYoki(byte[]);
}

-keep class sun.misc.Unsafe {
    public native void putObject(java.lang.Object, long, java.lang.Object);
    public long objectFieldOffset(java.lang.reflect.Field);
}

-keep, allowoptimization class com.sdk.helldroid.MainActivity {
    public native void ReplaceMethodByObject(java.lang.Object, java.lang.Object);
}

-keepclasseswithmembers, allowoptimization class com.sdk.helldroid.Helldroid {
    native <methods>;
}

-keepclassmembers, allowoptimization class com.sdk.helldroid.Helldroid {
    public static void LoadLibrary(android.content.Context);
    public static void registerNativeMethods(java.lang.Class);
    public static void createDummyFile(android.content.Context);
    public static void checkMainTrace();
}

-dontoptimize
-allowaccessmodification
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers