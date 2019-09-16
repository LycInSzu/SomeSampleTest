package com.cydroid.ota.storage;

/**
 * Created by borney on 4/14/15.
 */
public interface IStorage {
    void putInt(String key, int value);

    int getInt(String key, int defValue);

    void putLong(String key, long value);

    long getLong(String key, long defValue);

    void putFloat(String key, float value);

    float getFloat(String key, float defValue);

    void putBoolean(String key, boolean value);

    boolean getBoolean(String key, boolean defValue);

    void putString(String key, String value);

    String getString(String key, String defValue);

    void clear();

    void registerOnkeyChangeListener(OnKeyChangeListener listener);

    void unregisterOnkeyChangeListener(OnKeyChangeListener listener);

    interface OnKeyChangeListener {
        void onKeyChange(String key);
    }
}
