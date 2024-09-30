package sun.misc;

@SuppressWarnings({"unused", "rawtypes"})
public class Unsafe {
    public native void putObject(Object obj, long offset, Object newValue);
    public long objectFieldOffset(java.lang.reflect.Field field) {
        throw new RuntimeException("Stub!");
    }
}
