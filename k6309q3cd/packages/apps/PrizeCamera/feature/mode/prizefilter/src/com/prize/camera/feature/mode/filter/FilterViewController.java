package com.prize.camera.feature.mode.filter;

import android.content.Context;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.R;
import com.prize.camera.feature.mode.filter.uvfilter.UVFilterEntrance;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yangming.jiang on 2018/10/24.
 */

public class FilterViewController implements View.OnClickListener, IFilterDeviceController.PreviewCallback {

    private static final LogUtil.Tag TAG = new LogUtil.Tag(com.prize.camera.feature.mode.filter.FilterViewController.class.getName());
    private IApp mApp;
    private MainHandler mMainHandler;
    private static final int MSG_VIEW_INIT= 100;
    private static final int MSG_VIEW_SHOW = 101;
    private static final int MSG_VIEW_UNINIT_REMOVE = 102;
    private static final int MSG_SHOW_SUB_WINDOW = 103;
    private static final int MSG_HIDE_SUB_WINDOW = 104;
    private RelativeLayout mRootView;

    private UVFilterEntrance mUVFilterEntrance;
    private Context mContext;
    private boolean mSubWindowInited;

    private static final int [] FILTER_EFFECTS = new int[]{
        3, 8, 12, 22, 33, 44, 55, 66, 77, 88, 99, 57
    };

    public void init(IApp app, String cameraId, ICameraContext context){
        mApp = app;
        mContext = app.getActivity();
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mMainHandler.sendEmptyMessage(MSG_VIEW_INIT);
        mMainHandler.sendEmptyMessageDelayed(MSG_SHOW_SUB_WINDOW, 2000);
    }

    public void uninit(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessage(MSG_VIEW_UNINIT_REMOVE);
        }
    }

    public void showView(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessageDelayed(MSG_VIEW_SHOW,500);
        }
    }

    private void initView(){
        if (mRootView != null) {
            return;
        }

        mRootView = (RelativeLayout) mApp.getActivity().getLayoutInflater().inflate(R.layout.prize_filter_view,null);
        mUVFilterEntrance = new UVFilterEntrance(mRootView,mContext, mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.filter_item_width), mApp.getActivity().getResources().getDimensionPixelOffset(R.dimen.filter_item_height));
        mApp.getAppUi().getModeRootView().addView(mRootView);
        mRootView.setVisibility(View.GONE);

        mRootView.findViewById(R.id.sv_sml_prvw_0).setOnClickListener(this);
        mRootView.findViewById(R.id.sv_sml_prvw_1).setOnClickListener(this);
        mRootView.findViewById(R.id.sv_sml_prvw_2).setOnClickListener(this);
        mRootView.findViewById(R.id.sv_sml_prvw_3).setOnClickListener(this);
        mRootView.findViewById(R.id.sv_sml_prvw_4).setOnClickListener(this);
        mRootView.findViewById(R.id.sv_sml_prvw_5).setOnClickListener(this);
        mRootView.findViewById(R.id.sv_sml_prvw_6).setOnClickListener(this);
        mRootView.findViewById(R.id.sv_sml_prvw_7).setOnClickListener(this);
        mRootView.findViewById(R.id.sv_sml_prvw_8).setOnClickListener(this);
    }

    private void unitView(){

        if (mMainHandler != null){
            mMainHandler.removeMessages(MSG_VIEW_INIT);
        }

        if(null != mRootView){
            mApp.getAppUi().getModeRootView().removeView(mRootView);
            mRootView = null;
        }

        if(null != mUVFilterEntrance){
            mUVFilterEntrance.destroySP();
            mUVFilterEntrance.destroy();
            mUVFilterEntrance = null;
        }
    }

    public void showUVItemView(){
        if(null != mRootView){
            mRootView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int tag = Integer.parseInt((String) v.getTag());
        LogHelper.d(TAG, "zhangguo filter click="+tag);
        if(tag >= 0 && tag < FILTER_EFFECTS.length){
            updateSurface(tag);
        }
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

    private int count  = 0;

    @Override
    public void onPreviewFrame(byte[] data, int width, int height) {
        LogHelper.i(TAG, "zhangguo onpreviewframe width="+width+" height="+height+" viewWidth="+mApp.getAppUi().getSurfaceTextureView().getWidth()+" viewHeight="+mApp.getAppUi().getSurfaceTextureView().getHeight());
        if(mSubWindowInited && null != mUVFilterEntrance){

            if(count++ == 100){
                try{
                    FileOutputStream os = new FileOutputStream(new File("/sdcard/I420_1440_1088.yuv"));
                    os.write(data);
                    os.flush();
                    os.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }


            byte[] nv21 = new byte[data.length];

            I420ToNV21(data, nv21, width, height);

            LogHelper.i(TAG, "zhangguo onpreviewframe 2222");
            mUVFilterEntrance.update(nv21, width, height, ImageFormat.NV21);
        }
    }

    private class MainHandler extends Handler{
        public MainHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_VIEW_INIT:
                    initView();
                break;
                case MSG_VIEW_SHOW:
                    showUVItemView();
                    break;
                case MSG_VIEW_UNINIT_REMOVE:
                    unitView();
                    break;
                case MSG_SHOW_SUB_WINDOW:
                    initSubWindow();
                    break;
                case MSG_HIDE_SUB_WINDOW:
                    break;
                default:
                    break;
            }
        }
    }

    public void updateItemSurface(byte[] data, int width, int height, int format) {
        showUVItemView();
        if (null != mUVFilterEntrance) {
            mUVFilterEntrance.update(data, width, height, format);
        }
    }

    public void updateSurface(int id){
        if (null != mUVFilterEntrance) {
            mUVFilterEntrance.update(id);
        }
    }

    public UVFilterEntrance getUVFilterEntrance(){
        return mUVFilterEntrance;
    }

    public void initSubWindow(){
        if(null != mUVFilterEntrance){

            mUVFilterEntrance.init(mApp.getAppUi().getCameraId());

            mSubWindowInited = true;
        }
    }
}
