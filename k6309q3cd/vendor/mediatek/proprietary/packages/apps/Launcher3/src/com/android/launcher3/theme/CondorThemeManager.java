package com.cydroid.launcher3.theme;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class CondorThemeManager {
    private static final String TAG = "CondorThemeManager";

    private static final String THEME_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Condor Theme/Theme/";
    //private static final String DEFAULT_THEME_PATH = "/system//media//kb_resources/DefaultTheme/";
    private Bitmap mMaskBitmap=null;
    private Bitmap mShadowBitmap=null;
    private static CondorThemeManager sInstance = null;

    public Bitmap getmMaskBitmap() {
        Log.d(TAG, "------ getmMaskBitmap  ---------------");
        if (mMaskBitmap==null){
            Log.d(TAG, "------ mMaskBitmap==null  ---------------");
            sInstance.initOtherIconDrawable();
        }
        return mMaskBitmap;
    }

    public Bitmap getmShadowBitmap() {
        Log.d(TAG, "------ getmShadowBitmap  ---------------");
        if (mShadowBitmap==null){
            Log.d(TAG, "------ mShadowBitmap==null  ---------------");
            sInstance.initOtherIconDrawable();
        }
        return mShadowBitmap;
    }




    public static CondorThemeManager getInstance() {
        Log.d(TAG, "------ getInstance  ---------------");
        if (sInstance == null) {
            sInstance = new CondorThemeManager();
            sInstance.initOtherIconDrawable();
        }

        return sInstance;
    }

    public void clearShadowImage(){
        mShadowBitmap=null;
    }

    //for icons not in the theme
    private void initOtherIconDrawable() {
        Log.d(TAG, "------ initOtherIconDrawable  ---------------");
        mMaskBitmap = getBitmapNoResize("mask.jpg");
        mShadowBitmap = getBitmapNoResize("shadow.jpg");
        if (mMaskBitmap == null) {
            Log.d(TAG, "------ mMaskBitmap == null  ---------------");
            mMaskBitmap = mShadowBitmap;
        }
    }

    private Bitmap getBitmapNoResize(String file) {
        Log.d(TAG, "------ getBitmapNoResize  ---------------  file  is  "+file);
        Bitmap bitmap = getThemeIconBitmap(THEME_PATH, file);
        if (bitmap == null) {
//            bitmap = getBitmapNoResize(DEFAULT_THEME_PATH, file);
            Log.d(TAG, "------ getBitmapNoResize  --------------- the file  is  "+file+ "is  null");
        }
        return bitmap;
    }

    private Bitmap getThemeIconBitmap(String path, String file) {
        Log.d(TAG, "------ getThemeIconBitmap  ---------------  ");
        String pathName = THEME_PATH + file;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public Bitmap getIcon(ComponentName romComponentName) {
        Log.d(TAG, "------ getIcon  ---------------");
        Bitmap icon = this.getBitmapFromComponentDrawable(romComponentName);

        return icon;
    }




    public Bitmap getBitmapFromComponentDrawable(ComponentName componentName) {
        Log.d(TAG, "------ getBitmapFromComponentDrawable" );
        String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Condor Theme/" + "Theme/" + this.getImageName(componentName) + ".jpg";
//        String imagePathPreinstall = "/system//media//kb_resources//DefaultTheme/" + this.getImageName(componentName) + ".jpg";
        File imageFile = new File(imagePath);
//        File imageFilePreinstall = new File(imagePathPreinstall);

        Log.d(TAG, "------ getBitmapFromComponentDrawable  --------------- imagePath  is  "+imagePath);
        Bitmap myBitmap = null;
        if (imageFile.exists()) {
            Log.d(TAG, "------ getBitmapFromComponentDrawable   ---------------  got the bitmap");
            myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            return myBitmap;
        } /*else if(imageFilePreinstall.exists()) {
            myBitmap = BitmapFactory.decodeFile(imageFilePreinstall.getAbsolutePath());
        }*/
        Log.d(TAG, "------ getBitmapFromComponentDrawable   -------------  the bitmap  is   null------");
        return null;
    }

    private String getImageName(ComponentName componentName) {
        Log.d(TAG, "------ getImageName   the image name is " +componentName.getPackageName() + "_" + componentName.getClassName());
        return componentName.getPackageName() + "_" + componentName.getClassName();
    }






    private static final String DEFAULT_THEME_PATH = "/system//media//gn_resources/DefaultTheme/";
    public Bitmap getDefaulteIcon(ComponentName romComponentName) {
        Log.d(TAG, "------ getDefaulteIcon  ---------------");
        Bitmap icon = this.getDefaulteBitmapFromComponentDrawable(romComponentName);
        return icon;
    }

    public Bitmap getDefaulteBitmapFromComponentDrawable(ComponentName componentName) {
        Log.d(TAG, "------ getDefaulteBitmapFromComponentDrawable" );
        String imagePathPreinstall = "/system//media//gn_resources//DefaultTheme/" + this.getImageName(componentName) + ".png";
        File imageFile = new File(imagePathPreinstall);
//        File imageFilePreinstall = new File(imagePathPreinstall);

        Log.d(TAG, "------ getDefaulteBitmapFromComponentDrawable  --------------- imagePath  is  "+imagePathPreinstall);
        Bitmap myBitmap = null;
        if (imageFile.exists()) {
            Log.d(TAG, "------ getDefaulteBitmapFromComponentDrawable   ---------------  got the bitmap");
            myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            return myBitmap;
        } /*else if(imageFilePreinstall.exists()) {
            myBitmap = BitmapFactory.decodeFile(imageFilePreinstall.getAbsolutePath());
        }*/
        Log.d(TAG, "------ getDefaulteBitmapFromComponentDrawable   -------------  the bitmap  is   null------");
        return null;
    }







}
