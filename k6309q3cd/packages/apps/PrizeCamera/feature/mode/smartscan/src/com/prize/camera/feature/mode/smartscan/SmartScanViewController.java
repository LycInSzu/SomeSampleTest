package com.prize.camera.feature.mode.smartscan;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.camera.feature.mode.smartscan.view.ViewfinderView;
import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.R;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.widget.RotateImageView;

/**
 * Created by yangming.jiang on 2018/10/24.
 */

public class SmartScanViewController{

    private static final LogUtil.Tag TAG = new LogUtil.Tag(SmartScanViewController.class.getName());
    private IApp mApp;
    private MainHandler mMainHandler;
    private static final int VIEW_INIT_AND_SHOW = 100;
    private static final int VIEW_UNINIT_REMOVE = 101;
    private RelativeLayout mRootView;
    private RotateImageView mScanFromAlbum;
    private ViewfinderView mViewfinderView;
    private Point screenResolution;
    private View.OnClickListener mOnClickListener;

    public SmartScanViewController() {
    }


    public void init(IApp app, String cameraId, ICameraContext context,View.OnClickListener listener){
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mOnClickListener = listener;
    }

    public void uninit(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessage(VIEW_UNINIT_REMOVE);
        }
    }

    public void showView(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessageDelayed(VIEW_INIT_AND_SHOW,500);
        }
    }

    private void initView(){
        if (mRootView != null) {
            return;
        }
        mRootView = (RelativeLayout) mApp.getActivity().getLayoutInflater().inflate(R.layout.smart_scan_view,null);
        mViewfinderView = (ViewfinderView)mRootView.findViewById(R.id.viewfinder_content);
        mScanFromAlbum = (RotateImageView)mRootView.findViewById(R.id.scan_album);
        mScanFromAlbum.setOnClickListener(mOnClickListener);
        WindowManager manager = (WindowManager)mApp.getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        screenResolution = new Point(display.getWidth(), display.getHeight());
        mViewfinderView.setmPreviewFrame(getFramingRect());
        mApp.getAppUi().getModeRootView().addView(mRootView);

    }

    private void unitView(){
        mApp.getAppUi().getModeRootView().removeView(mRootView);
        mRootView = null;
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

    private Rect getFramingRect() {
        Rect framingRect = null;
        if (screenResolution == null)
            return null;
        if (framingRect == null) {
            //修改之后
            int width = screenResolution.x * 7 / 10;
            int height = screenResolution.y * 7 / 10;

            if (height >= width) { //竖屏
                height = width;
            } else { //黑屏
                width = height;
            }
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 3;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);

        }
        return framingRect;
    }

    public void drawViewfinder(){
        if (mViewfinderView != null){
            mViewfinderView.drawViewfinder();
        }
    }

}
