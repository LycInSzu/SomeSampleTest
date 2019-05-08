package com.prize.camera.feature.mode.pano;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.widget.ScaleAnimationButton;
import com.mediatek.camera.R;
import com.mediatek.camera.common.relation.DataStore;
import com.ymsl.uvpanorama.UVPanorama.UVPanoUtils.UVPanoHolder;
import com.ymsl.uvpanorama.UVPanorama.Panorama.IUVPanoCallback;
import com.ymsl.uvpanorama.UVPanorama.UVPanoramaInterface;
import com.ymsl.uvpanorama.UVPanorama.UVPanoramaUI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by yangming.jiang on 2018/10/24.
 */

public class PanoViewController implements IPanoDeviceController.PreviewCallback, IAppUi.TexutreUpdateCallback {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(com.prize.camera.feature.mode.pano.PanoViewController.class.getName());
    private IApp mApp;
    private MainHandler mMainHandler;
    private static final int VIEW_INIT_AND_SHOW = 100;
    private static final int VIEW_UNINIT_REMOVE = 101;
    private static final int MSG_UPDATE_TIPS = 103;
    private static final int MSG_CAPTURE = 104;
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


    private static final int MERGE_STATE_NORMAL = 0;
    private static final int MERGE_STATE_TOO_FAST = 1;
    private static final int MERGE_STATE_TOO_UP = 2;
    private static final int MERGE_STATE_TOO_DOWN = 3;
    private int mCurStatus = -1;
    private TextView mTipView;
    private int mPreviewWidth = 1280;
    private WorkHandler mWorkHandler;

    private class WorkHandler extends Handler{

        public WorkHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_CAPTURE){
                if(null != msg.obj){
                    parserNv21Data((byte[])msg.obj, msg.arg1, mPreviewWidth, msg.arg2);
                }
            }
        }
    }

    public interface OnPanoStatuCallback{
        void onCaptureStopClick();
        void onCaptureDataReciver(Bitmap result);
        void onCaptureFull(boolean isfull);
        void onPlaySound();
    }

    public void init(IApp app, String cameraId, ICameraContext context){
        mApp = app;
        mCameraId = cameraId;
        mContext = app.getActivity();
        mDataStore = context.getDataStore();
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());

        /*if (mWorkHandler == null) {
            HandlerThread t = new HandlerThread("pano-work-thread");
            t.start();
            mWorkHandler = new WorkHandler(t.getLooper());
        }*/

        if(!cameraId.equals("0")){
            mPanoCapDirection = UVPanoHolder.RIGHT_TO_LEFT;
        }
    }

    private void initPanoramaUI(RelativeLayout root) {
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        mUVPanoramaUI = new UVPanoramaUI(mApp.getActivity(), root, dm.widthPixels, dm.heightPixels, 1 == mApp.getAppUi().getCameraId());
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
        LogHelper.d(TAG, "pano crash destroy 0000");
        if (null != mUVPanoramaUI) {
            LogHelper.d(TAG, "pano crash destroy 1111");
            mUVPanoramaUI.destroySmallPreview();
            LogHelper.d(TAG, "pano crash destroy 2222");
            mUVPanoramaUI = null;
        }
    }

    private void initThumbPreview(int width, int height, int format) {
        if (null != mUVPanoramaUI) {
            LogHelper.d(TAG,"initThumbPreview width = " + width + ", height = " + height );
            boolean isFront = "1".equals(mCameraId);
            mUVPanoramaUI.initThumbPreview(width, height, format, isFront ? UVPanoHolder.RIGHT_TO_LEFT : UVPanoHolder.LEFT_TO_RIGHT, isFront);
        } else {
            LogHelper.e(TAG, "initThumbPreview null == mUVPanoramaUI err");
        }
    }

    synchronized public void uninit(){
        /*if (mMainHandler != null){
            mMainHandler.sendEmptyMessage(VIEW_UNINIT_REMOVE);
        }*/
        if(null != mWorkHandler){
            mWorkHandler.removeCallbacksAndMessages(null);
            mWorkHandler.getLooper().quit();
            mWorkHandler = null;
        }

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
        mTipView = (TextView)mRootView.findViewById(R.id.textview_tip);
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

    private static Bitmap nv21ToBitmap2(byte[] nv21, int width, int height, int targetWidth) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, targetWidth, height), 90, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void parserNv21Data(byte[] nv21, int width, int height, int targetWidth){
        LogHelper.d(TAG, "pano parserNv21Data start width="+width+" height="+height+" targetWidth="+targetWidth);

        final Bitmap bitmap1 = nv21ToBitmap2(nv21,  targetWidth, height, width);

        LogHelper.d(TAG, "pano parserNv21Data 2");
        if(null != mOnPanoStatuCallback){
            mOnPanoStatuCallback.onPlaySound();
        }

        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap2 = rotateBitmap(bitmap1, 1 == mApp.getAppUi().getCameraId() ? 270 : 90);

                LogHelper.d(TAG, "pano parserNv21Data 3");

                if(mOnPanoStatuCallback != null){
                    mOnPanoStatuCallback.onCaptureDataReciver(bitmap2);
                }
            }
        }, 20);
    }

    private class PanoCaptureData implements UVPanoramaInterface.PanoPictureCallback {

        @Override
        public void onData(byte[] data, int width, int height, int targetWidth) {
            if (mOnPanoStatuCallback != null){
                LogHelper.d(TAG,"onData");
                if(null != mWorkHandler){
                    Message msg = mWorkHandler.obtainMessage();
                    msg.what = MSG_CAPTURE;
                    msg.obj = data;
                    msg.arg1 = width;
                    msg.arg2 = height;
                    msg.sendToTarget();
                }else{
                    parserNv21Data(data, width, height, targetWidth);
                }
            }
        }
    }

    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    @Override
    synchronized public void onPreviewFrame(final byte[] data, final int width, final int height) {

        mPreviewWidth = width;

        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCaptureStart){
                    if (!mIsThumbPreviewInited) {
                        mIsThumbPreviewInited = true;
                        initThumbPreview(width, height, ImageFormat.NV21);
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
        mCurStatus = -1;
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCaputreRootView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void stopCapture(){
        mIsThumbPreviewInited = false;
        mCurStatus = -1;
        LogHelper.d(TAG, "pano stopCapture stopCapture");
        if(null != mTipView){
            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTipView.setText(null);
                }
            });
        }

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
        updateSmallPreview(mApp.getAppUi().getSurfaceTextureView());
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
                case  MSG_UPDATE_TIPS:
                    if(null != mTipView){
                        mCurStatus = msg.arg1;
                        if(msg.arg1 == MERGE_STATE_NORMAL){
                            mTipView.setText(R.string.pano_status_normal);
                        }else if(msg.arg1 == MERGE_STATE_TOO_FAST){
                            mTipView.setText(R.string.pano_status_slow_down);
                        }else if(msg.arg1 == MERGE_STATE_TOO_UP){
                            mTipView.setText(R.string.pano_status_move_down);
                        }else if(msg.arg1 == MERGE_STATE_TOO_DOWN){
                            mTipView.setText(R.string.pano_status_move_up);
                        }
                    }
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

            if(mCaptureStart && status >= UVPanoHolder.STS_PANO_CAPTURE_WIDTH_FULL_STOP
                    && status != UVPanoHolder.STS_PANO_CAPTURE_UP_OUT_STOP
                    && status != UVPanoHolder.STS_PANO_CAPTURE_DOWN_OUT_STOP){
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
            int mergestatus = 0;

            if(!mCaptureStart){
                return;
            }

            //LogHelper.d(TAG, "onShakeStatus status="+status+" shakeX="+shakeX);
            if (status == UVPanoHolder.STATUS_LITTLE_QUICK) {
                mergestatus = MERGE_STATE_TOO_FAST;
            } else {
                mergestatus = MERGE_STATE_NORMAL;
            }

            if (Math.abs(shakeX) > SHAKE_LEVEL) {
                //strId = (shakeX < 0) ? "too up" : "too down";
                mergestatus = (shakeX < 0) ? MERGE_STATE_TOO_UP : MERGE_STATE_TOO_DOWN;
            }

            if(mergestatus != mCurStatus){
                Message msg = mMainHandler.obtainMessage();
                msg.what = MSG_UPDATE_TIPS;
                msg.arg1 = mergestatus;
                msg.sendToTarget();
            }

            //Log.e(TAG, "onShakeStatus strId:" + strId);
        }
    }
}
