package com.cydroid.ota.storage;

import android.content.SharedPreferences;

import com.cydroid.ota.execption.SettingUpdateRuntimeException;
import com.cydroid.ota.Log;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by borney on 4/14/15.
 */
public abstract class StorageAdapter implements IStorage {
    private final String TAG = getClass().getSimpleName();
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private List<SoftReference<OnKeyChangeListener>> mListeners = Collections.synchronizedList(new ArrayList<SoftReference<OnKeyChangeListener>>());

    protected StorageAdapter(SharedPreferences preferences) {
        mSharedPreferences = preferences;
        mEditor = preferences.edit();
    }

    @Override
    public synchronized void putLong(String key, long value) {
        mEditor.putLong(key, value);
    }

    @Override
    public synchronized long getLong(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    @Override
    public synchronized void putInt(String key, int value) {
            mEditor.putInt(key, value).commit();
    }

    @Override
    public synchronized int getInt(String key, int defValue) {
            return mSharedPreferences.getInt(key, defValue);
    }

    @Override
    public synchronized void putFloat(String key, float value) {
            mEditor.putFloat(key, value).commit();
    }

    @Override
    public synchronized float getFloat(String key, float defValue) {
            return mSharedPreferences.getFloat(key, defValue);
    }

    @Override
    public synchronized void putBoolean(String key, boolean value) {
            mEditor.putBoolean(key, value).commit();
    }

    @Override
    public synchronized boolean getBoolean(String key, boolean defValue) {
            return mSharedPreferences.getBoolean(key, defValue);
    }

    @Override
    public synchronized void putString(String key, String value) {
            mEditor.putString(key, value).commit();
    }

    @Override
    public synchronized String getString(String key, String defValue) {
            return mSharedPreferences.getString(key, defValue);
    }

    @Override
    public synchronized void clear() {
        mEditor.clear().commit();
    }

    @Override
    public void registerOnkeyChangeListener(OnKeyChangeListener listener) {
        Log.d(TAG, "registerOnkeyChangeListener");
        if (mListeners.size() == 0) {
            mSharedPreferences.registerOnSharedPreferenceChangeListener(mSharedChangeListener);
        }
        mListeners.add(new SoftReference<OnKeyChangeListener>(listener));
    }

    @Override
    public void unregisterOnkeyChangeListener(OnKeyChangeListener listener) {
        Iterator<SoftReference<OnKeyChangeListener>> iterator = mListeners.iterator();
        while (iterator.hasNext()) {
            SoftReference<OnKeyChangeListener> listenerRef = iterator.next();
            if (listenerRef != null) {
                OnKeyChangeListener onKeyChangeListener = listenerRef.get();

                if (onKeyChangeListener != null && onKeyChangeListener.equals(listener)) {
                    Log.d(TAG, "unregisterOnkeyChangeListener , break  ");
                    iterator.remove();
                    break;
                }
            }
        }
        if (mListeners.size() == 0) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mSharedChangeListener);
        }
        Log.d(TAG, "unregisterOnkeyChangeListener");
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mSharedChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "sharedPreferences = " + sharedPreferences + " key = " + key);
            Iterator<SoftReference<OnKeyChangeListener>> iterator = mListeners.iterator();
            while (iterator.hasNext()) {
                Log.d(TAG, "mListeners size: " + mListeners.size());
                SoftReference<OnKeyChangeListener> listenerRef = iterator.next();
                int index = mListeners.indexOf(listenerRef);
                OnKeyChangeListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onKeyChange(key);
                } else {
                    iterator.remove();
                }

                if (!mListeners.contains(listenerRef)) {
                    iterator = mListeners.iterator();
                    while (iterator.hasNext() && index > 0) {
                        iterator.next();
                        index--;
                    }
                }
            }
            Log.d(TAG, "onSharedPreferenceChanged>>>>" + Thread.currentThread().getName() + " notify end ");
        }
    };
}
