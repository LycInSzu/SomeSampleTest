package com.goodix.fingerprint.setting.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by tangjie on 17-7-19.
 */

public class DataPrefDefault {
    private static DataPrefDefault sInstance;
    private Context mContext;
    private PreferenceManager mPreferenceManager;
    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;
    private DataPrefDefault(Context context){
        mSp = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mSp.edit();
    }

    public static DataPrefDefault getInstance(Context context){
        if(null == sInstance){

            sInstance = new DataPrefDefault(context);
        }
        return sInstance;


    }



    public int size(){
        int size = 0;

        size = mSp.getAll().size();

        return size;
    }

    public Map<String,?> dataItems(){
        return  mSp.getAll();
    }

    public  void putString(String key, String value){
        mEditor.putString(key,value);
        mEditor.commit();
    }

    public void putInt(String key, int value){
        mEditor.putInt(key,value);
        mEditor.commit();
    }

    public String getString(String key, String def){

        return mSp.getString(key,def);

    }

    public int getInt(String key, int def){
        return  mSp.getInt(key,def);
    }

    public  void remove(String key) {

        mSp.edit();
        mEditor.remove(key);
        SharedPreferencesCompat.apply(mEditor);
    }

    static class SharedPreferencesCompat
    {
        private static final Method sApplyMethod = findApplyMethod();

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static Method findApplyMethod(){
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e){
            }
            return null;
        }

        public static void apply(SharedPreferences.Editor editor){
            try {
                if (sApplyMethod != null){
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e){
            } catch (IllegalAccessException e){
            } catch (InvocationTargetException e){
            }
            editor.commit();
        }
    }
}

