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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.prize.PrizeRelativeLayout;
import com.mediatek.camera.common.widget.ScaleAnimationButton;
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
    private static final int VIEW_INIT_AND_SHOW = 100;
    private static final int VIEW_UNINIT_REMOVE = 101;
    private RelativeLayout mRootView;
    private String mDefaultType = "0";
    private String mCameraId;
    private Context mContext;
    private DataStore mDataStore;
    private boolean mCaptureStart = false;
    //private UVPanoJni mPanoJni;
    private UVPanoramaUI mUVPanoramaUI;
    private int mPanoCapDirection = UVPanoHolder.LEFT_TO_RIGHT;
    private boolean mIsThumbPreviewInited = false;
    private UVPanoCallbackImpl mUVPanoCallbackImpl = new UVPanoCallbackImpl();
    private ScaleAnimationButton mStopCaptureView;
    private OnPanoStatuCallback mOnPanoStatuCallback;
    private RelativeLayout mCaputreRootView;
    private static final int SHAKE_LEVEL = 100;
    public interface OnPanoStatuCallback{
        void onCaptureStopClick();
        void onCaptureDataReciver(Bitmap result);
        void onCaptureFull(boolean isfull);
    }

    public void init(IApp app, String cameraId, ICameraContext context){
        mApp = app;
        mCameraId = cameraId;
        mContext = app.getActivity();
        mDataStore = context.getDataStore();
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        if(!cameraId.equals("0")){
            mPanoCapDirection = UVPanoHolder.RIGHT_TO_LEFT;
        }

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
            mUVPanoramaUI.initSamllPreview(mContext, mApp.getAppUi().getCameraId());
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
        /*mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mUVPanoramaUI) {
                    LogHelper.d(TAG, "pano crash destroy 1111");
                    mUVPanoramaUI.destroySmallPreview();
                    LogHelper.d(TAG, "pano crash destroy 1111");
                    mUVPanoramaUI = null;
                } else {
                    LogHelper.e(TAG, "destroyPanoramaUI null == mUVPanoramaUI err");
                }
            }
        });*/

        LogHelper.d(TAG, "pano crash destroy 0000");
        if (null != mUVPanoramaUI) {
            LogHelper.d(TAG, "pano crash destroy 1111");
            mUVPanoramaUI.destroySmallPreview();
            LogHelper.d(TAG, "pano crash destroy 2222");
            mUVPanoramaUI = null;
        }

    }

    private void initThumbPreview(int width, int height, int format, int orientation) {
        if (null != mUVPanoramaUI) {
            LogHelper.d(TAG,"initThumbPreview width = " + width + ", height = " + height );
            mUVPanoramaUI.initThumbPreview(width, height, format, orientation);
        } else {
            LogHelper.e(TAG, "initThumbPreview null == mUVPanoramaUI err");
        }
    }

    synchronized public void uninit(){
        /*if (mMainHandler != null){
            mMainHandler.sendEmptyMessage(VIEW_UNINIT_REMOVE);
        }*/
        LogHelper.d(TAG, "pano crash uninit 1111");
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogHelper.d(TAG, "pano crash uninit 3333");
                unitView();
                LogHelper.d(TAG, "pano crash uninit 4444");
            }
        });
        LogHelper.d(TAG, "pano crash uninit 2222");
        destroySmallPreview();
        mIsThumbPreviewInited = false;
        //mPanoJni.nativeUnInit();
    }

    public void showView(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessage(VIEW_INIT_AND_SHOW);
        }
    }

    synchronized private void initView(){
        if (mRootView != null) {
            return;
        }

        mRootView = (RelativeLayout) mApp.getActivity().getLayoutInflater().inflate(R.layout.panorama_layout,null);
        mApp.getAppUi().getModeRootView().addView(mRootView);
        initPanoramaUI(mRootView.findViewById(R.id.panorama_layout));
        mCaputreRootView = (RelativeLayout) mRootView.findViewById(R.id.pano_container);
        mStopCaptureView = (ScaleAnimationButton) mRootView.findViewById(R.id.pano_stop_shutter);
        mStopCaptureView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mOnPanoStatuCallback != null){
                    mOnPanoStatuCallback.onCaptureStopClick();
                }
            }
        });
        mApp.getAppUi().setTexutreUpdateCallback(this);
    }

    private void unitView(){
        //stopCapture();
        mIsThumbPreviewInited = false;
        mCaptureStart = false;

        mApp.getAppUi().setTexutreUpdateCallback(null);
        if(null != mCaputreRootView){
            mCaputreRootView.setVisibility(View.GONE);
        }

        if(null != mUVPanoramaUI){
            mUVPanoramaUI.stopThumbPreview();
        }

        if (mMainHandler != null){
            mMainHandler.removeMessages(VIEW_INIT_AND_SHOW);
        }

        if (null != mRootView){
            mApp.getAppUi().getModeRootView().removeView(mRootView);
            mRootView = null;
        }
    }

    private void setThumbPreviewInteractive(UVPanoramaInterface.PanoPictureCallback panoPictureCallback) {
        if (null != mUVPanoramaUI) {
            mUVPanoramaUI.setThumbPreviewInteractive(mUVPanoCallbackImpl, panoPictureCallback);
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

    private void stopThumbPreview() {
        if (null != mUVPanoramaUI) {
            mUVPanoramaUI.stopThumbPreview();
        } else {
            LogHelper.e(TAG, "updateThumbPreview null == mUVPanoramaUI err");
        }
    }

    private class PanoCaptureData implements UVPanoramaInterface.PanoPictureCallback {

        @Override
        public void onData(Bitmap bitmap) {
            if (mOnPanoStatuCallback != null){
                LogHelper.d(TAG,"  onData = " + bitmap);
                mOnPanoStatuCallback.onCaptureDataReciver(bitmap);
            }
        }
    }

    @Override
    synchronized public void onPreviewFrame(final byte[] data, final int width, final int height) {

        LogHelper.i(TAG, "zhangguo pano onPreviewFrame");

        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCaptureStart){
                    if (!mIsThumbPreviewInited) {
                        mIsThumbPreviewInited = true;
                        initThumbPreview(width, height, ImageFormat.NV21, mPanoCapDirection);
                        setThumbPreviewInteractive(new PanoCaptureData());
                    }

                    byte[] nv21 = new byte[data.length];
                    I420ToNV21(data, nv21, width, height);
                    startThumbPreview(nv21);
                }
            }
        });
    }

    private void I420ToNV21(final byte[] input, final byte[] output, final int width, final int height) {
        //long startMs = System.currentTimeMillis();
        final int frameSize = width * height;
        final int qFrameSize = frameSize / 4;
        final int tempFrameSize = frameSize * 5 / 4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i * 2] = input[tempFrameSize + i]; // Cb (U)
            output[frameSize + i * 2 + 1] = input[frameSize + i]; // Cr (V)
        }
    }

    public void startCapture(){
        mCaptureStart = true;

        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCaputreRootView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void stopCapture(){
        mIsThumbPreviewInited = false;

        if(mCaptureStart){
            mCaptureStart = false;
            stopThumbPreview();
        }
    }

    public void hideCaptureView(){

        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCaputreRootView.setVisibility(View.GONE);
            }
        });
    }

    public void setOnPanoStatuCallback(OnPanoStatuCallback listener){
        mOnPanoStatuCallback = listener;
    }

    @Override
    public void onTextureUpdated(SurfaceTexture surfaceTexture) {
        //Log.i("zg", "zhangguo pano onTextureUpdated");
        updateSmallPreview(mApp.getAppUi().getSurfaceTextureView());
        //Log.i("zg", "zhangguo pano onTextureUpdated 2222");
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

            if(status >= UVPanoHolder.STS_PANO_CAPTURE_WIDTH_FULL_STOP){
                if(null != mOnPanoStatuCallback){
                    mApp.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOnPanoStatuCallback.onCaptureFull(UVPanoHolder.STS_PANO_CAPTURE_WIDTH_FULL_STOP == status);
                        }
                    });
                }
            }
        }

        @Override
        public void onShakeStatus(int status, int shakeX) {
            /*String strId;

            if (status == UVPanoHolder.STATUS_LITTLE_QUICK) {
                strId = "too fast";
            } else {
                strId = "normal";
            }

            if (Math.abs(shakeX) > SHAKE_LEVEL) {
                strId = (status < 0) ? "too up" : "too down";
            }*/

            //Log.e(TAG, "onShakeStatus strId:" + strId);
        }
    }
}
