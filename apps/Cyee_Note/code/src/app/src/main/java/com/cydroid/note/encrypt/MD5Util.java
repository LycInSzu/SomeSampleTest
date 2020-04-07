package com.cydroid.note.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static byte[] convertMD5(byte[] content) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5.digest(content);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static void encrypt(byte[] content, byte[] key) {
        encrypt(content, 0, content.length, key);
    }

    public static void encrypt(byte[] content, int offset, int count, byte[] key) {
        xor(content, offset, count, key);
        rotateLeft(content, offset, count, 3);
    }

    public static void decrypt(byte[] ciphertext, byte[] key) {
        decrypt(ciphertext, 0, ciphertext.length, key);
    }

    public static void decrypt(byte[] content, int from, int count, byte[] keys) {
        rotateLeft(content, from, count, 5);
        xor(content, from, count, keys);
    }

    private static void xor(byte[] content, int from, int count, byte[] keys) {
        int md5Length = keys.length;
        int contentLength = from + count;
        for (int i = from, j = 0; i < contentLength; i++, j++) {
            if (j >= md5Length) {
                j = 0;
            }
            content[i] = (byte) (content[i] ^ keys[j]);
        }
    }

    private static void rotateLeft(byte[] data, int from, int count, final int shiftLeft) {
        final int shiftRight = 8 - shiftLeft;
        for (int i = from, end = from + count; i < end; ++i) {
            int b = data[i] & 0xFF;
            data[i] = (byte) ((b << shiftLeft) | (b >>> shiftRight));
        }
    }
}
