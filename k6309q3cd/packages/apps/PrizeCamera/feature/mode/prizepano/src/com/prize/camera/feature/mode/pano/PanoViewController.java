package com.prize.camera.feature.mode.pano;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.R;
import com.mediatek.camera.common.relation.DataStore;
import com.ymsl.uvpanorama.UVPanorama.UVPanoUtils.FileUtils;
import com.ymsl.uvpanorama.UVPanorama.UVPanoUtils.UVPanoHolder;
import com.ymsl.uvpanorama.UVPanorama.Panorama.IUVPanoCallback;
import com.ymsl.uvpanorama.UVPanorama.UVPanoramaInterface;
import com.ymsl.uvpanorama.UVPanorama.UVPanoramaUI;

/**
 * Created by yangming.jiang on 2018/10/24.
 */

public class PanoViewController implements IPanoDeviceController.PreviewCallback, IAppUi.TexutreUpdateCallback {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(com.prize.camera.feature.mode.pano.PanoViewController.class.getName());
    private IApp mApp;
    private MainHandler mMainHandler;
    public static final String PORTRAIT_KEY = "lava_portrait_key";
    private static final int VIEW_INIT_AND_SHOW = 100;
    private static final int VIEW_UNINIT_REMOVE = 101;
    private RelativeLayout mRootView;
    private TextView mTipsView;
    private TypedArray mPortraitItemIconArray = null;
    private TypedArray mPortraitItemSmallIconArray = null;
    private String mPortraitTitles [] = null;
    private String mDefaultType = "0";
    private String mCurrentType = mDefaultType;
    private String mCameraId;
    private ViewItemChanedListener mViewItemChanedListener;
    private Context mContext;
    private DataStore mDataStore;
    private boolean isViewShow = false;
    //private UVPanoJni mPanoJni;
    private UVPanoramaUI mUVPanoramaUI;
    private int mPanoCapDirection = UVPanoHolder.LEFT_TO_RIGHT;
    private boolean mIsThumbPreviewInited = false;
    private UVPanoCallbackImpl mUVPanoCallbackImpl = new UVPanoCallbackImpl();
    public void init(IApp app, String cameraId, ICameraContext context){
        mApp = app;
        mCameraId = cameraId;
        mContext = app.getActivity();
        mDataStore = context.getDataStore();
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        /*mPanoJni = new UVPanoJni();
        mPanoJni.nativeInit(1440, 1080, 1440 * 5, 160, 120);
        mPanoJni.nativeProcess(100, 100, new byte[10]);*/

        LogHelper.d(TAG, "zhangguo process end");

    }

    private void initPanoramaUI(RelativeLayout root) {
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        mUVPanoramaUI = new UVPanoramaUI(mApp.getActivity(), root, dm.widthPixels, dm.heightPixels);
        initSmallPreview();
    }

    private void initSmallPreview() {
        if (null != mUVPanoramaUI) {
            mUVPanoramaUI.initSamllPreview(mContext);
        } else {
            LogHelper.e(TAG, "initSmallPreview null == mUVPanoramaUI err");
        }
    }

    private void updateSmallPreview(Object object) {
        if (null != mUVPanoramaUI) {
            mUVPanoramaUI.updateSmallPreview(object);
        } else {
            LogHelper.e(TAG, "updateSmallPreview null == mUVPanoramaUI err");
        }
    }


    private void destroySmallPreview() {
        if (null != mUVPanoramaUI) {
            mUVPanoramaUI.destroySmallPreview();
        } else {
            LogHelper.e(TAG, "destroyPanoramaUI null == mUVPanoramaUI err");
        }
    }

    private void initThumbPreview(int width, int height, int format, int orientation) {
        if (null != mUVPanoramaUI) {
            mUVPanoramaUI.initThumbPreview(width, height, format, orientation);
        } else {
            LogHelper.e(TAG, "initThumbPreview null == mUVPanoramaUI err");
        }
    }


    public void uninit(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessage(VIEW_UNINIT_REMOVE);
        }
        destroySmallPreview();
        mIsThumbPreviewInited = false;
        //mPanoJni.nativeUnInit();
    }

    public void setCurrentCameraId(String cameraId){
        mCameraId = cameraId;
    }

    public void showView(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessage(VIEW_INIT_AND_SHOW);
        }
    }

    private void initView(){
        if (mRootView != null) {
            return;
        }
        mRootView = (RelativeLayout) mApp.getActivity().getLayoutInflater().inflate(R.layout.panorama_layout,null);
        mApp.getAppUi().getModeRootView().addView(mRootView);
        initPanoramaUI(mRootView);
        mApp.getAppUi().setTexutreUpdateCallback(this);
    }

    private void unitView(){

        mApp.getAppUi().setTexutreUpdateCallback(null);

        if (mMainHandler != null){
            mMainHandler.removeMessages(VIEW_INIT_AND_SHOW);
        }

        if (null != mRootView){
            mApp.getAppUi().getModeRootView().removeView(mRootView);
            mRootView = null;
        }
    }

    public void updateTipsView(int type){
        if (mTipsView != null){
            mTipsView.setText(mPortraitTitles[type]);
        }
    }

    public void setViewItemChanedListener(ViewItemChanedListener listenser){
        mViewItemChanedListener = listenser;
    }

    private void setThumbPreviewInteractive(Bitmap rightArrow, Bitmap leftArrow,
                                            UVPanoramaInterface.PanoPictureCallback panoPictureCallback) {
        if (null != mUVPanoramaUI) {
            mUVPanoramaUI.setThumbPreviewInteractive(rightArrow, leftArrow, mUVPanoCallbackImpl, panoPictureCallback);
        } else {
            LogHelper.e(TAG, "setThumbPreviewInteractive null == mUVPanoramaUI err");
        }
    }

    private void startThumbPreview(Object object) {
        if (null != mUVPanoramaUI) {
            mUVPanoramaUI.startThumbPreview(object);
        } else {
            LogHelper.e(TAG, "updateThumbPreview null == mUVPanoramaUI err");
        }
    }

    private class PanoCaptureData implements UVPanoramaInterface.PanoPictureCallback {

        @Override
        public void onData(Bitmap bitmap) {

        }
    }

    @Override
    public void onPreviewFrame(byte[] data, int width, int height) {
        Log.i("zg", "zhangguo pano previewframe");

        if (!mIsThumbPreviewInited) {
            mIsThumbPreviewInited = true;
            initThumbPreview(width, height, ImageFormat.YV12, mPanoCapDirection);
            setThumbPreviewInteractive(null, null, new PanoCaptureData());
        }

        startThumbPreview(data);
    }

    @Override
    public void onTextureUpdated(SurfaceTexture surfaceTexture) {
        updateSmallPreview(surfaceTexture);
    }

    public interface ViewItemChanedListener{
        void onViewItemChanged(int value);
    }

    private class MainHandler extends Handler{
        public MainHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case VIEW_INIT_AND_SHOW:
                    initView();
                break;
                case VIEW_UNINIT_REMOVE:
                    unitView();
                    break;
                default:
                    break;
            }
        }
    }

    private class UVPanoCallbackImpl implements IUVPanoCallback {

        @Override
        public void onStatus(int status, String msg) {
            LogHelper.e(TAG, "UVPanoCallbackImpl onStatus:" + status);

            switch (status) {
                case UVPanoHolder.STS_PANO_CAPTURE_STOP_CMD:
                    //sendMessage(UVPanoHolder.STS_PANO_CAPTURE_STOP_CMD);
                    break;

                case UVPanoHolder.STS_PANO_CAPTURE_OPPOSITE_DIRECTION_STOP:
                    //sendMessage(UVPanoHolder.STS_PANO_CAPTURE_STOP_CMD);
                    break;
            }
        }
    }
}
