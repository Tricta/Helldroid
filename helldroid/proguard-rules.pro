-keepclasseswithmembers, allowoptimization class com.lib.helldroid.Helldroid {
    native <methods>;
}

-keepclassmembers, allowoptimization class com.lib.helldroid.Helldroid {
    public static void LoadLibrary(android.content.Context, java.lang.Class);
    public static void registerNativeMethods(java.lang.Class);
    public static void createDummyFile(android.content.Context);
    public static void checkMainTrace();
}