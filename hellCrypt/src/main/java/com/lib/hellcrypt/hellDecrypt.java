package com.lib.hellcrypt;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class hellDecrypt {
    public static String hellYoki(byte[] cypherBytes) {
        return mathDec(xorDec(randomDec(Base64.getDecoder().decode(cypherBytes))));
    }

    private static String xorDec(byte[] cypherBytes) {
        byte[] decryptBytes = cypherBytes.clone();
        for (int i = 0, j = 0; i < decryptBytes.length; i++) {
            decryptBytes[i] ^= config.key[j];
            j = (j + 1) % config.key.length;
        }
        return new String(decryptBytes, StandardCharsets.UTF_8);
    }

    private static byte[] randomDec(byte[] inputStr) {
        Random random = new Random(config.seed);
        byte[] transform = new byte[inputStr.length];
        for (int i = 0; i < inputStr.length; i++) {
            transform[i] = (byte) (inputStr[i] ^ (random.nextInt(255) % (i + 10)));
        }

        return transform;
    }

    private static String mathDec(String inputStr) {
        StringBuilder result = new StringBuilder();

        for (char charValue : inputStr.toCharArray()) {
            int intValue = charValue;
            int sub = (intValue - 10) + (30 % 26);
            char newChar = (char) sub;
            result.append(newChar);
        }

        return result.toString();
    }
}
