/*
 * star os gif shot add by zhangguo 20150408
 * 
 * */
package com.prize.camera.feature.mode.gif;


import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;

import com.prize.camera.feature.mode.gif.encode.AnimatedGifEncoder;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.photo.ThumbnailHelper;
import com.mediatek.camera.common.utils.BitmapCreator;

import java.io.File;
import java.io.FileOutputStream;


public class GifMerge implements Runnable {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(GifMerge.class.getSimpleName());

    public int GIF_MERGE_RESULT_OK = 0;
    public int GIF_MERGE_RESULT_OOM = -1;
    public int GIF_MERGE_RESULT_OPEN_FAILED = -2;
    public int GIF_MERGE_RESULT_BITMAP_NULL = -3;

    private static final int MAX_TRY_COUNT = 150;

    private static final String TEMP_FILE_END = ".tmp";
    private static final String MIME_TYPE = "image/gif";

    private String mFilePath;
    private int mDelay;
    private boolean mCanRun = true;
    private GifCallback mCallback;
    private boolean mInited = false;
    private int mWidth, mHeight;

//  private byte[][] mImages = null;
    private byte[] mImages = null;
    private byte[] mFirstImages = null;
    private boolean[] mSingle = null;
    private Bitmap mCurrentBitmap;
    private Bitmap mFirstBitmap;
    private int mAddCursor;
    private int mMaxSize;
    private int mReadCursor;
    private Activity mContext;
    private IApp mApp;
    private Location mLocation;
    private String mFileName;
    private int mOrientation = -1;
    //private CameraActivity mCameraActivity;
    private int mRealSize = -1;
    private boolean mHavaStoped = false;

    //GifEncoder mGifEncoder;
    AnimatedGifEncoder mGifEncoder;
    //BitmapFactory.Options mOptions;

    private boolean mExit = false;
    int mTryCount;
    private boolean mCanSave = true;
    private boolean mComplete;
    private boolean isCurrentMerged = false;
    private static final long MAX_DUAL_TIME = 6*1000;
    private long mProcessedTime = 0l;

    public int getGIF_MERGE_RESULT_BITMAP_NULL() {
        return GIF_MERGE_RESULT_BITMAP_NULL;
    }

    public GifMerge(IApp app, String path, String fileName, int delay, int maxSize, int w, int h, GifCallback callback, Location location, int orientation){
        mApp = app;
        mFilePath = path;
        mDelay = delay;
        mCallback = callback;
        mSingle = new boolean[maxSize];
        mAddCursor = 0;
        mReadCursor = 0;
        mMaxSize = maxSize;
        if(orientation == 90 || orientation == 270){
            mWidth = h;
            mHeight = w;
        }else{
            mWidth = w;
            mHeight = h;
        }

        mLocation = location;
        mFileName = fileName;
        mContext = mApp.getActivity();
        //mGifEncoder = new GifEncoder();
        mGifEncoder = new AnimatedGifEncoder();
        //mGifEncoder.setQuality(3);
        
        /*mOptions = new BitmapFactory.Options();
        mOptions.inJustDecodeBounds = false;
        mOptions.inDither = false;
        mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;*/
        mProcessedTime = 0;
        mOrientation = orientation;
    }
    
    /*public void setCameraActivity(CameraActivity activity){
        
        mCameraActivity = activity;
    }*/
    
    // if stoped capture when taking gif pic, we set the real pic size to merge a gif
    public boolean exitCapture(){
        mExit = true;
        mCallback = null;
        mProcessedTime = 0;
        
        if(!mComplete){
            return true;
        }
        
        return false;
    }
    


    public void addBitmap(Bitmap bitmap){
        if(null != bitmap && mProcessedTime < MAX_DUAL_TIME && mCurrentBitmap == null){

            if(-1 == mOrientation){

                //mOrientation = Exif.getOrientation(bitmap);
            }

            mCurrentBitmap = bitmap;
            isCurrentMerged = false;

            LogHelper.d(TAG, "prize gif shot addBitmap mAddCursor="+ mProcessedTime/1000);
        }else{
            LogHelper.d(TAG, "prize gif shot addBitmap error");
        }
    }

    public void updateProcessTime(long time){
        mProcessedTime = time;
    }
    
    public void Stop(){
        
        if(!mHavaStoped){
            LogHelper.d(TAG, "gif Stop");
            mHavaStoped = true;
            mCanRun = false;
            mProcessedTime = 0;
            //mSingle = new boolean[mMaxSize];
            try {
                mGifEncoder.finish();
            } catch (Exception e) {
                // TODO: handle exception
                
            }
        }
        
        mComplete = true;
    }

    private Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
        }
        return null;

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
        while (mCanRun) {

            
            if(mProcessedTime< MAX_DUAL_TIME && !isCurrentMerged  && mCurrentBitmap != null){
                if(!mInited){
                    boolean result = false;
                    try {
                        File file = new File(mFilePath + TEMP_FILE_END);
                        if(!file.exists()){
                            file.createNewFile();
                        }
                        
                        result = mGifEncoder.start(new FileOutputStream(file));
                        mGifEncoder.setRepeat(0);
                        mGifEncoder.setDelay(mDelay);
                    } catch (Exception e) {
                        // TODO: handle exception
                        LogHelper.e(TAG, "gif start error:"+e);
                        onGifEncodeError(false);
                        return;
                    }
                    
                    if(!result){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        continue;
                    }else{
                        mInited = true;
                    }
                }else {
                   Bitmap bitmap = mCurrentBitmap;
                    boolean result = false;
                    long addStartTime = System.currentTimeMillis();
                    LogHelper.d(TAG, "AddFrame start ");

                    if(mOrientation != 0){
                        bitmap = adjustPhotoRotation(bitmap, mOrientation);
                    }

                    if(null == mFirstBitmap){
                        mFirstBitmap = bitmap;
                    }

                    LogHelper.d(TAG, "AddFrame rotate end ");
                    try {
                        result = mGifEncoder.addFrame(bitmap);
                    } catch (Exception e) {
                        // TODO: handle exception
                        LogHelper.e(TAG, "AddFrame error:"+e);
                        onGifEncodeError(false);
                        return;
                    }
                    
                    LogHelper.d(TAG, "AddFrame end cost time =  " + (System.currentTimeMillis() - addStartTime));
                    mCurrentBitmap = null;
                    isCurrentMerged = true;
                    mReadCursor++;
                    
                    if(null != mCallback){
                        mCallback.onGifMergeComplete(result, mReadCursor);
                    }
                    
                    if(mProcessedTime>= MAX_DUAL_TIME || mExit || !mCanSave){
                        Stop();
                        break;
                    }
                }
            }else if(mExit || !mCanSave){
                Stop();
                break;
            }
            
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        if(mCanSave){
            saveImageToDatabase();
        }else{
            delFailedFile();
        }
        
        mImages = null;
        
        if(null != mCallback){
            mCallback.onSaveComplete(mCanSave);
        }
        
        mComplete = true;
        
        mCallback = null;
    }
    
    private long reNameToRealFile(){
        
        File file = new File(mFilePath + TEMP_FILE_END);
        
        if(!file.exists() || file.length() == 0){
            return 0;
        }
        
        long length = file.length();
        
        file.renameTo(new File(mFilePath));
        
        return length;
    }
    
    private void saveImageToDatabase() {
        // Insert into MediaStore.
        long size = reNameToRealFile();
        
        if(size < 1){
            return;
        }
        
        ContentValues values = new ContentValues(14);
        values.put(ImageColumns.TITLE, mFileName.substring(0, mFileName.indexOf('.')));
        values.put(ImageColumns.DISPLAY_NAME, mFileName);
        values.put(ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(ImageColumns.MIME_TYPE, MIME_TYPE);
        values.put(ImageColumns.DATA, mFilePath);
        values.put(ImageColumns.SIZE, size);
        if (mLocation != null) {
            values.put(ImageColumns.LATITUDE, mLocation.getLatitude());
            values.put(ImageColumns.LONGITUDE, mLocation.getLongitude());
        }
        values.put(ImageColumns.ORIENTATION, 0);

        // Add for Refocus image database
        //values.put(Images.Media.CAMERA_REFOCUS, 0);
        values.put(ImageColumns.WIDTH, mWidth);
        values.put(ImageColumns.HEIGHT, mHeight);
        try {
            Uri mUri = mContext.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
            if (mUri != null) {
                mApp.notifyNewMedia(mUri,true);
                mApp.getAppUi().setMirrorEnable(false);
                Bitmap thumbmailBitmap =  zoomBitmap(mFirstBitmap, ThumbnailHelper.getThumbnailWidth(), ThumbnailHelper.getThumbnailHeight());
                mApp.getAppUi().updateThumbnail(thumbmailBitmap);

                /*
                mCameraActivity.lavaAddSecureAlbumItemIfNeeded(false, mUri);
                LogHelper.i(TAG, "[saveImageToDatabase]mUri = " + mUri);
                ThumbnailViewManager manager = ((CameraAppUiImpl)mCameraActivity.getCameraAppUI()).getThumbnailViewManager();
                int ratio = (int) Math.ceil((double) mHeight / manager.getThumbWidth());
                int inSampleSize = Integer.highestOneBit(ratio);
                manager.onGifSaved(Thumbnail.createThumbnail(mFirstImages, mOrientation, inSampleSize, mUri, mFilePath));*/
            }
            
        } catch (IllegalArgumentException e) {
            LogHelper.e(TAG,
                    "[saveImageToDatabase]Failed to write MediaStore,IllegalArgumentException:",
                    e);
        } catch (UnsupportedOperationException e) {
            LogHelper.e(TAG,
                    "[saveImageToDatabase]Failed to write MediaStore,UnsupportedOperationException:",
                    e);
        }
        
        mFirstImages = null ; // added by zhangwt
        
    }
    
    public void waitDone() {
            
        while (!mHavaStoped) {

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
    }
    
    
    public interface GifCallback{
        public void onInitComplete(boolean result);
        public void onGifMergeComplete(boolean result, int piccount);
        public void onSaveComplete(boolean success);
    }
    
    private void onGifEncodeError(boolean close){
        mHavaStoped = true;
        mCanRun = false;
        mCanSave = false;
        mSingle = new boolean[mMaxSize];
        mProcessedTime = 0;
        mCurrentBitmap = null;
        isCurrentMerged = false;
        if(!close && null != mGifEncoder){
            try {
                mGifEncoder.finish();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        
        delFailedFile();
        
        if(null != mCallback){
            mCallback.onGifMergeComplete(false, mReadCursor);
        }
        mCallback = null;
    }
    
    private void delFailedFile(){
        File file = new File(mFilePath + TEMP_FILE_END);
        if(file.exists()){
            file.deleteOnExit();
        }
    }
    
    public void onSaveTimeOut(){
        mCanSave = false;
        mCallback = null;
    }
    
    public void resetCallback(){
        mCallback = null;
    }

    private static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidht, scaleHeight, x, y;
        Bitmap newbmp;
        Matrix matrix = new Matrix();
        if (width > height) {
            scaleWidht = ((float) h / height);
            scaleHeight = ((float) h / height);
            x = (width - w * height / h) / 2;
            y = 0;
        } else if (width < height) {
            scaleWidht = ((float) w / width);
            scaleHeight = ((float) w / width);
            x = 0;
            y = (height - h * width / w) / 2;
        } else {
            scaleWidht = ((float) w / width);
            scaleHeight = ((float) w / width);
            x = 0;
            y = 0;
        }
        matrix.postScale(scaleWidht, scaleHeight);
        try {
            newbmp = Bitmap.createBitmap(bitmap, (int) x, (int) y, (int) (width - x), (int) (height - y), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return newbmp;
    }
}



