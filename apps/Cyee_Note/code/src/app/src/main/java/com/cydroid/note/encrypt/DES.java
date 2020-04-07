package com.cydroid.note.encrypt;

import android.text.TextUtils;
import com.cydroid.note.common.Log;

import org.apache.commons.codec.binary.Base64;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DES {

    private Key mKey;
    private Cipher mCipher;

    public static final String OPERATION_DECODE = "DECODE";
    public static final String OPERATION_ENCODE = "ENCODE";
    public static final String DES_KEY = "a1m2i3g4o5n6o7t8e";

    public DES() {
    }

    private static DES getInstance(String key) throws NoSuchPaddingException,
            NoSuchAlgorithmException {
        return getInstance(getKeyByStr(key));
    }

    private static DES getInstance(byte key[]) throws NoSuchPaddingException,
            NoSuchAlgorithmException {
        DES des = new DES();
        if (des.mKey == null) {
            SecretKeySpec spec = new SecretKeySpec(key, "DES");
            des.mKey = spec;
        }
        des.mCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        return des;
    }

    private byte[] encrypt(byte b[]) throws InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException,
            IllegalStateException {
        byte byteFina[] = null;
        mCipher.init(Cipher.ENCRYPT_MODE, mKey);
        byteFina = mCipher.doFinal(b);
        return byteFina;
    }

    private byte[] decrypt(byte b[]) throws InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException,
            IllegalStateException {
        byte byteFina[] = null;
        mCipher.init(Cipher.DECRYPT_MODE, mKey);
        byteFina = mCipher.doFinal(b);
        return byteFina;
    }

    private static byte[] getKeyByStr(String str) {
        byte bRet[] = new byte[str.length() / 2];
        for (int i = 0, len = str.length() / 2; i < len; i++) {
            Integer itg = Integer.valueOf(16 * getChrInt(str.charAt(2 * i))
                    + getChrInt(str.charAt(2 * i + 1)));
            bRet[i] = itg.byteValue();
        }
        return bRet;
    }

    private static int getChrInt(char chr) {
        int iRet = 0;
        if (chr == "0".charAt(0))
            iRet = 0;
        if (chr == "1".charAt(0))
            iRet = 1;
        if (chr == "2".charAt(0))
            iRet = 2;
        if (chr == "3".charAt(0))
            iRet = 3;
        if (chr == "4".charAt(0))
            iRet = 4;
        if (chr == "5".charAt(0))
            iRet = 5;
        if (chr == "6".charAt(0))
            iRet = 6;
        if (chr == "7".charAt(0))
            iRet = 7;
        if (chr == "8".charAt(0))
            iRet = 8;
        if (chr == "9".charAt(0))
            iRet = 9;
        if (chr == "A".charAt(0))
            iRet = 10;
        if (chr == "B".charAt(0))
            iRet = 11;
        if (chr == "C".charAt(0))
            iRet = 12;
        if (chr == "D".charAt(0))
            iRet = 13;
        if (chr == "E".charAt(0))
            iRet = 14;
        if (chr == "F".charAt(0))
            iRet = 15;
        return iRet;
    }

    private String encrypt(String text, String keyString) {
        String body = null;
        try {
            DES des = DES.getInstance(keyString);
            byte[] b = des.encrypt(text.getBytes("UTF8"));
            body = new String(Base64.encodeBase64(b));//NOSONAR
        } catch (Exception ex) {
            Log.i("", "encrypt ex = " + ex);
        }
        return body;
    }

    private String decrypt(String text, String keyString) {
        String body = null;
        try {
            DES des = DES.getInstance(keyString);
            byte[] b = Base64.decodeBase64(text.getBytes("UTF8"));
            body = new String(des.decrypt(b), "UTF8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }

    public String authcode(String content, String operation, String key) {
        String encontent = null;
        if (!TextUtils.isEmpty(operation)
                && operation.equals(OPERATION_ENCODE)) {
            encontent = encrypt(content, key);
        } else if (!TextUtils.isEmpty(operation)
                && operation.equals(OPERATION_DECODE)) {
            encontent = decrypt(content, key);
        }
        return encontent;
    }

}