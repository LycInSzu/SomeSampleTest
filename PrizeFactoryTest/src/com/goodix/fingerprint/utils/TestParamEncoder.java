/*
 * Copyright (C) 2013-2016, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.fingerprint.utils;

public class TestParamEncoder {
    private static final String TAG = "TestParamEncoder";

    public static final int TEST_ENCODE_SIZEOF_INT8 = (Integer.SIZE + Byte.SIZE) / 8;
    public static final int TEST_ENCODE_SIZEOF_INT16 = (Integer.SIZE + Short.SIZE) / 8;
    public static final int TEST_ENCODE_SIZEOF_INT32 = (Integer.SIZE + Integer.SIZE) / 8;
    public static final int TEST_ENCODE_SIZEOF_INT64 = (Integer.SIZE + Long.SIZE) / 8;
    public static final int TEST_ENCODE_SIZEOF_FLOAT = (Integer.SIZE + Integer.SIZE + Float.SIZE) / 8;
    public static final int TEST_ENCODE_SIZEOF_DOUBLE = (Integer.SIZE + Integer.SIZE + Double.SIZE) / 8;

    public static final int testEncodeSizeOfArray(int len) {
        return ( (Integer.SIZE + Integer.SIZE) / 8 + len);
    }

    public static int encodeInt8(byte[] buf, int offset, int key, byte value) {
        if (null == buf) {
            return offset;
        }

        // encode key
        buf[offset++] = (byte) (key & 0xFF);
        buf[offset++] = (byte) ((key >> 8) & 0xFF);
        buf[offset++] = (byte) ((key >> 16) & 0xFF);
        buf[offset++] = (byte) ((key >> 24) & 0xFF);

        // encode value
        buf[offset++] = value;

        return offset;
    }

    public static int encodeInt16(byte[] buf, int offset, int key, short value) {
        if (null == buf) {
            return offset;
        }

        // encode key
        buf[offset++] = (byte) (key & 0xFF);
        buf[offset++] = (byte) ((key >> 8) & 0xFF);
        buf[offset++] = (byte) ((key >> 16) & 0xFF);
        buf[offset++] = (byte) ((key >> 24) & 0xFF);

        // encode value
        buf[offset++] = (byte) (value & 0xFF);
        buf[offset++] = (byte) ((value >> 8) & 0xFF);

        return offset;
    }

    public static int encodeInt32(byte[] buf, int offset, int key, int value) {
        if (null == buf) {
            return offset;
        }

        // encode key
        buf[offset++] = (byte) (key & 0xFF);
        buf[offset++] = (byte) ((key >> 8) & 0xFF);
        buf[offset++] = (byte) ((key >> 16) & 0xFF);
        buf[offset++] = (byte) ((key >> 24) & 0xFF);

        // encode value
        buf[offset++] = (byte) (value & 0xFF);
        buf[offset++] = (byte) ((value >> 8) & 0xFF);
        buf[offset++] = (byte) ((value >> 16) & 0xFF);
        buf[offset++] = (byte) ((value >> 24) & 0xFF);

        return offset;
    }

    public static int encodeInt64(byte[] buf, int offset, int key, long value) {
        if (null == buf) {
            return offset;
        }

        // encode key
        buf[offset++] = (byte) (key & 0xFF);
        buf[offset++] = (byte) ((key >> 8) & 0xFF);
        buf[offset++] = (byte) ((key >> 16) & 0xFF);
        buf[offset++] = (byte) ((key >> 24) & 0xFF);

        // encode value
        buf[offset++] = (byte) (value & 0xFF);
        buf[offset++] = (byte) ((value >> 8) & 0xFF);
        buf[offset++] = (byte) ((value >> 16) & 0xFF);
        buf[offset++] = (byte) ((value >> 24) & 0xFF);
        buf[offset++] = (byte) ((value >> 32) & 0xFF);
        buf[offset++] = (byte) ((value >> 40) & 0xFF);
        buf[offset++] = (byte) ((value >> 48) & 0xFF);
        buf[offset++] = (byte) ((value >> 56) & 0xFF);

        return offset;
    }

    public static int encodeFloat(byte[] buf, int offset, int key, float value) {
        if (null == buf) {
            return offset;
        }

        // encode key
        buf[offset++] = (byte) (key & 0xFF);
        buf[offset++] = (byte) ((key >> 8) & 0xFF);
        buf[offset++] = (byte) ((key >> 16) & 0xFF);
        buf[offset++] = (byte) ((key >> 24) & 0xFF);

        // encode size
        int size = 4;
        buf[offset++] = (byte) (size & 0xFF);
        buf[offset++] = (byte) ((size >> 8) & 0xFF);
        buf[offset++] = (byte) ((size >> 16) & 0xFF);
        buf[offset++] = (byte) ((size >> 24) & 0xFF);

        //encode value
        int valueInt = Float.floatToIntBits(value);
        buf[offset++] = (byte) (valueInt & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 8) & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 16) & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 24) & 0xFF);

        return offset;
    }

    public static int encodeDouble(byte[] buf, int offset, int key, double value) {
        if (null == buf) {
            return offset;
        }

        // encode key
        buf[offset++] = (byte) (key & 0xFF);
        buf[offset++] = (byte) ((key >> 8) & 0xFF);
        buf[offset++] = (byte) ((key >> 16) & 0xFF);
        buf[offset++] = (byte) ((key >> 24) & 0xFF);

        // encode size
        int size = 8;
        buf[offset++] = (byte) (size & 0xFF);
        buf[offset++] = (byte) ((size >> 8) & 0xFF);
        buf[offset++] = (byte) ((size >> 16) & 0xFF);
        buf[offset++] = (byte) ((size >> 24) & 0xFF);

        // encode value
        long valueInt = Double.doubleToLongBits(value);
        buf[offset++] = (byte) (valueInt & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 8) & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 16) & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 24) & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 32) & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 40) & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 48) & 0xFF);
        buf[offset++] = (byte) ((valueInt >> 56) & 0xFF);

        return offset;
    }

    public static int encodeArray(byte[] buf, int offset, int key, byte[] array, int size) {
        if (null == buf || null == array) {
            return offset;
        }

        // encode key
        buf[offset++] = (byte) (key & 0xFF);
        buf[offset++] = (byte) ((key >> 8) & 0xFF);
        buf[offset++] = (byte) ((key >> 16) & 0xFF);
        buf[offset++] = (byte) ((key >> 24) & 0xFF);

        // encode size
        buf[offset++] = (byte) (size & 0xFF);
        buf[offset++] = (byte) ((size >> 8) & 0xFF);
        buf[offset++] = (byte) ((size >> 16) & 0xFF);
        buf[offset++] = (byte) ((size >> 24) & 0xFF);

        // encode array
        System.arraycopy(array, 0, buf, offset, size);
        offset += size;

        return offset;
    }
}