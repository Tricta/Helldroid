package com.lib.hellcrypt;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public final class Stub {
    public static final Stub instance = new Stub();

    private final Method hellYoker;

    Stub() {
        String dummy1 = _decrypt(new byte[0]);
        String dummy2 = _decrypt(new byte[0]);
        hellYoker = Stub.class.getDeclaredMethods()[0];
    }

    private static String _decrypt(byte[] cypherBytes) {
        byte[] stub = cypherBytes.clone();
        for (int i = 1; i < stub.length; i++) {
            stub[i] ^= stub[i - 1];
        }
        return new String(stub, StandardCharsets.UTF_8);
    }

    public String hellYoki(String encrypted) {
        try {
            return (String) hellYoker.invoke(null, encrypted.getBytes(StandardCharsets.UTF_8));
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
