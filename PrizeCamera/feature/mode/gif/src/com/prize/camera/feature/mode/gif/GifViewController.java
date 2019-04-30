package com.prize.camera.feature.mode.gif;

import android.content.Context;
import android.content.res.TypedArray;
import android.icu.text.DecimalFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mediatek.camera.common.ICameraContext;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.R;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.widget.ScaleAnimationButton;

/**
 * Created by yangming.jiang on 2018/10/24.
 */

public class GifViewController{

    private static final LogUtil.Tag TAG = new LogUtil.Tag(GifViewController.class.getName());
    private IApp mApp;
    private MainHandler mMainHandler;
    private static final int VIEW_INIT_AND_SHOW = 100;
    private static final int VIEW_UNINIT_REMOVE = 101;
    private RelativeLayout mRootView;
    private TextView mTipsView;
    private SeekBar mProcessSeekBar;
    private String mCameraId;
    private Context mContext;
    private DataStore mDataStore;
    private boolean isViewShow = false;
    private static final long MAX_DUAL_TIME = 6*1000;
    private static final String MAX_DUAL_TIME_TIPS = "6s";
    private ScaleAnimationButton mStopRecordingView;
    private OnGifStopClickListener mOnGifStopClickListener;

    public interface OnGifStopClickListener{
        void onGifStopClick();
    }


    public void init(IApp app, String cameraId, ICameraContext context){
        mApp = app;
        mCameraId = cameraId;
        mContext = app.getActivity();
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
    }

    public void uninit(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessage(VIEW_UNINIT_REMOVE);
        }
    }

    public void setCurrentCameraId(String cameraId){
        mCameraId = cameraId;
    }
    public void showView(){
        if (mMainHandler != null){
            mMainHandler.sendEmptyMessageDelayed(VIEW_INIT_AND_SHOW,50);
        }
    }

    private void initView(){
        if (mRootView != null) {
            return;
        }
        mRootView = (RelativeLayout) mApp.getActivity().getLayoutInflater().inflate(R.layout.prize_gif_view,null);
        mTipsView = (TextView) mRootView.findViewById(R.id.gif_tips);
        mProcessSeekBar = (SeekBar) mRootView.findViewById(R.id.gif_process_bar);
        mStopRecordingView = (ScaleAnimationButton) mRootView.findViewById(R.id.gif_stop_shutter);
        mStopRecordingView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mOnGifStopClickListener != null){
                    mOnGifStopClickListener.onGifStopClick();
                }
            }
        });
        mProcessSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion) {
                // disable seek bar
                return true;
            }
        });
        mApp.getAppUi().getModeRootView().addView(mRootView);
        mProcessSeekBar.setProgress(0);
        updateProgress(0);
    }

    public void updateProgress(long processTime){
        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int progress = calSeekbarProgress(processTime);
                if (mProcessSeekBar != null){
                    mProcessSeekBar.setProgress(progress);
                }
                reFreshTipsView(processTime);
            }
        });
    }

    DecimalFormat mFormat = new DecimalFormat("0.0");

    private void reFreshTipsView(long processTime){
        String costTime = mFormat.format(processTime/1000f);
        String processTips = costTime + "s/"+ MAX_DUAL_TIME_TIPS;
        if (mTipsView != null){
            mTipsView.setText(processTips);
        }
    }

    private int calSeekbarProgress(long processTime){
        int progress = (int) (processTime*100f/MAX_DUAL_TIME);
        return progress;
    }

    private void unitView(){

        mApp.getAppUi().getModeRootView().removeView(mRootView);
        mRootView = null;
    }

    public void setOnGifStopClickListener(OnGifStopClickListener listener){
        mOnGifStopClickListener = listener;
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
}
