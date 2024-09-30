package com.lib.hellcrypt;

public final class config {
    public static byte[] key;
    public static final long seed;

    static {
        key = new byte[] { 121, 111, 107, 105, 79, 102, 90, 101, 122, 101 };
        seed = 245;
    }
}
