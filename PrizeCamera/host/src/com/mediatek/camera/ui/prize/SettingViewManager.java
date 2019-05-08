package com.mediatek.camera.ui.prize;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.camera.CameraActivity;
import com.mediatek.camera.common.IAppUi;
import com.mediatek.camera.common.IAppUiListener;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.SwitchPreference;
import com.mediatek.camera.common.relation.DataStore;
import com.mediatek.camera.common.setting.ICameraSettingView;
import com.mediatek.camera.R;
import com.mediatek.camera.common.utils.CameraUtil;
import com.mediatek.camera.prize.PrizeLifeCycle;
import com.mediatek.camera.ui.CameraAppUI;

import java.util.ArrayList;
import java.util.List;

public class SettingViewManager implements GridView.OnItemClickListener , IAppUiListener.OnGestureListener ,IApp.OnTouchListener, IApp.BackPressedListener, PrizeSettingSubView.PrizeSubSettingCallback {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(SettingViewManager.class.getSimpleName());

    private List<ICameraSettingView> mSettingViewList = new ArrayList<>();
    private List<ICameraSettingView> mTargetList = new ArrayList<>();
    private IApp mApp;
    private IAppUi mAppUI;
    private CameraActivity mActivity;
    //private DataStore mDataStore;
    private ViewGroup mRootView;
    private LinearLayout mSettingContainer;
    private LinearLayout mSettingView;
    private GridView mGridView;
    private GridAdapter mAdapter;
    private OnOrientationChangeListenerImpl mOrientationChangeListener;
    private int mItemWidth;
    private int mMinMarginTop;
    private PrizeSettingSubView mSubViewFrame;
    private View mLastSelectView;
    private int mScreenWidth;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == mTargetList.size()){
            resetSettings();
            return;
        }

        PrizeCameraSettingView settingView = (PrizeCameraSettingView)mTargetList.get(position);

        if(!settingView.isEnabled()){
            return;
        }

        if(null != mLastSelectView){
            ViewHolder holder = (ViewHolder)mLastSelectView.getTag();
            holder.mSummeryView.setPressed(false);
        }

        if(settingView.getSettingType() == PrizeCameraSettingView.SETTING_TYPE_SWITCH){
            boolean on = PrizeCameraSettingView.VALUES_ON.equals(settingView.getValue());
            if(on){
                settingView.onValueChanged(PrizeCameraSettingView.VALUES_OFF);
            }else{
                settingView.onValueChanged(PrizeCameraSettingView.VALUES_ON);
            }

            mAdapter.notifyDataSetChanged();

            hideSubMenu();

            mLastSelectView = null;
        }else{
            if(mLastSelectView != view || null == mLastSelectView){
                showSubMenu(position);
                mLastSelectView = view;
                ViewHolder holder = (ViewHolder)mLastSelectView.getTag();
                holder.mSummeryView.setPressed(true);
            }else{
                hideSubMenu();
                mLastSelectView = null;
            }
        }
    }

    private void resetSettings(){
        LogHelper.i(TAG, "[onClick] showResetDialog...");
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(R.string.camera_setting_reset_title)
                .setMessage(R.string.camera_setting_reset_message)
                .setNegativeButton(mActivity.getResources()
                        .getString(R.string.setting_dialog_cancel),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(mActivity.getResources()
                        .getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogHelper.i(TAG, "[onClick] yes...");
                        DataStore mDataStore = new DataStore(mActivity);
                        mDataStore.resetSettingsData();
                        mActivity.reset();
                        IAppUi appUi = mActivity.getAppUi();
                        hideSettings();
                    }
                }).create();
        dialog.show();
    }

    private void showSubMenu(int position){
        //mSubViewFrame
        PrizeCameraSettingView settingView = (PrizeCameraSettingView)mTargetList.get(position);
        mSubViewFrame.setSettingView(mApp, settingView);
        mSubViewFrame.setVisibility(View.VISIBLE);
    }

    private void hideSubMenu(){
        mSubViewFrame.setVisibility(View.GONE);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(float x, float y) {
        return hideSettings();
    }

    @Override
    public boolean onSingleTapConfirmed(float x, float y) {
        return false;
    }

    @Override
    public boolean onDoubleTap(float x, float y) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    @Override
    public boolean onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    @Override
    public boolean onLongPress(float x, float y) {
        return false;
    }

    @Override
    public boolean onTouch(MotionEvent event) {

        if(null != event && null != mGridView){

            LogHelper.d(TAG, "zhangguo eventy="+event.getRawY()+" gridy="+mGridView.getY());

            if(event.getRawY() < mGridView.getY() && event.getRawX() < mScreenWidth - mScreenWidth / 5){
                hideSettings();
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if(isSettingShowing()){

            hideSettings();
            return true;
        }

        return false;
    }

    @Override
    public void onSubSettingChanged() {
        if(null != mAdapter){
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ViewHolder{
        //ImageView mIconView;
        TextView mSummeryView;
    }

    private class GridAdapter extends BaseAdapter{

        private LayoutInflater mInflater;
        public GridAdapter(Context context){
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mTargetList.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(null == convertView){
                convertView = mInflater.inflate(R.layout.prize_setting_item, parent, false);

                ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(mItemWidth, mItemWidth);
                convertView.setLayoutParams(param);

                holder = new ViewHolder();
                //holder.mIconView = (ImageView) convertView.findViewById(R.id.grid_image_view);
                holder.mSummeryView = (TextView) convertView.findViewById(R.id.grid_text_view);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            if(position == mTargetList.size()){
                holder.mSummeryView.setText(R.string.camera_setting_reset_title);
                holder.mSummeryView.setCompoundDrawablesWithIntrinsicBounds(null,
                        mActivity.getResources().getDrawable(R.drawable.prize_selector_reset), null, null);
                return convertView;
            }

            PrizeCameraSettingView settingView = (PrizeCameraSettingView)mTargetList.get(position);

            LogHelper.i(TAG, "zhangguo view="+settingView.getClass().getName());

            List<String> values = settingView.getEntryValues();

            int selectIndex = 0;

            for(int i = 0; i < values.size(); i++){
                if(settingView.getValue().equals(values.get(i))){
                    selectIndex = i;
                    break;
                }
            }

            //holder.mIconView.setImageResource(getIcons()[selectIndex]);
            holder.mSummeryView.setEnabled(settingView.isEnabled());
            holder.mSummeryView.setText(settingView.getTitle());
            holder.mSummeryView.setCompoundDrawablesWithIntrinsicBounds(null,
                    mActivity.getResources().getDrawable(settingView.getIcons()[selectIndex]), null, null);

            return convertView;
        }
    }

    public SettingViewManager(IApp app){
        mApp = app;
        mAppUI = app.getAppUi();
        mActivity = (CameraActivity) app.getActivity();
        mOrientationChangeListener = new OnOrientationChangeListenerImpl();
        mItemWidth = (int) mActivity.getResources().getDimension(R.dimen.prize_setting_icon_size);
        mMinMarginTop = (int)mActivity.getResources().getDimension(R.dimen.top_bar_height);
        mScreenWidth = mActivity.getWindowManager().getDefaultDisplay().getWidth();
    }

    public boolean isSettingShowing(){
        if(null != mRootView && null != mSettingContainer){
            return mRootView.getVisibility() == View.VISIBLE;
        }
        return false;
    }

    public void showSettings(){

        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(null == mRootView || null == mSettingContainer){
                    mRootView = mActivity.findViewById(R.id.setting_ui_root);
                    mSettingContainer = mRootView.findViewById(R.id.setting_container);
                }

                if(null == mSettingView){
                    mSettingView = (LinearLayout) mApp.getActivity().getLayoutInflater().inflate(R.layout.prize_setting_layout,null);
                    mGridView = (GridView) mSettingView.findViewById(R.id.grid_view);
                    mSubViewFrame = (PrizeSettingSubView)mSettingView.findViewById(R.id.sub_setting_layout);
                    mSubViewFrame.setCallback(SettingViewManager.this);
                    mRootView.findViewById(R.id.prize_setting_container).setBackground(null);
                    mRootView.findViewById(R.id.prize_setting_actionbar).setVisibility(View.GONE);
                    mSettingContainer.addView(mSettingView);
                    mGridView.setOnItemClickListener(SettingViewManager.this);
                }

                updateVisibleSettings();

                if(null == mAdapter){
                    mAdapter = new GridAdapter(mActivity);
                }

                mGridView.setAdapter(mAdapter);

                mRootView.setVisibility(View.VISIBLE);

                synchronized (this) {
                    for (ICameraSettingView view : mTargetList) {
                        view.refreshView();
                    }
                }

                mApp.registerOnOrientationChangeListener(mOrientationChangeListener);
                mAppUI.registerGestureListener(SettingViewManager.this, 0);
                mApp.registerOnTouchListener(SettingViewManager.this);
                mApp.registerBackPressedListener(SettingViewManager.this, IApp.DEFAULT_PRIORITY);
            }
        });
    }

    public boolean hideSettings(){

        if(null != mRootView){

            if(mRootView.getVisibility() == View.GONE){
                return false;
            }

            mApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRootView.setVisibility(View.GONE);
                }
            });
        }

        mApp.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mApp.unregisterOnOrientationChangeListener(mOrientationChangeListener);

                if(null != mSettingContainer){
                    mSettingContainer.removeAllViews();
                }

                mSettingView = null;

                synchronized (this){
                    for (ICameraSettingView view : mTargetList) {
                        if (view instanceof PrizeLifeCycle){
                            ((PrizeLifeCycle) view).onPause();
                        }
                    }
                }

                mAppUI.unregisterGestureListener(SettingViewManager.this);
                mApp.unregisterOnTouchListener(SettingViewManager.this);
                mActivity.unRegisterBackPressedListener(SettingViewManager.this);
            }
        });


        return true;
    }

    public synchronized void addSettingView(ICameraSettingView view) {
        LogHelper.i(TAG, "[addSettingView], view:" + view);
        if (view == null) {
            LogHelper.w(TAG, "[addSettingView], view:" + view, new Throwable());
            return;
        }

        if(view instanceof PrizeCameraSettingView){
            synchronized (this){
                if (!mSettingViewList.contains(view)) {

                    int curIndex = 0;
                    for(int i = 0; i < mSettingViewList.size(); i++){
                        PrizeCameraSettingView v = (PrizeCameraSettingView) mSettingViewList.get(i);
                        if(v.getOrder() > ((PrizeCameraSettingView) view).getOrder()){
                            break;
                        }else{
                            curIndex = i;
                        }
                    }
                    mSettingViewList.add(curIndex, view);
                    //mSettingViewList.add(view);
                }
            }
        }else{
            LogHelper.w(TAG, "addSettingView not PrizeCameraSettingView");
        }
    }

    /**
     * Remove setting view instance from setting view list.
     *
     * @param view The instance of {@link ICameraSettingView}.
     */
    public synchronized void removeSettingView(ICameraSettingView view) {
        synchronized (this){
            mSettingViewList.remove(view);
        }
    }

    /**
     * Refresh setting view.
     */
    public synchronized void refreshSettingView() {
        for (ICameraSettingView view : mSettingViewList) {
            view.refreshView();
        }
    }

    public synchronized void updateVisibleSettings() {
        ArrayList<ICameraSettingView> target = new ArrayList<>();
        for (ICameraSettingView view : mSettingViewList) {
            if(view.isEnabled()){
                target.add(view);
            }
        }

        mTargetList = target;
        if(null != mAdapter){
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Whether setting view tree has any visible child or not. True means it has at least
     * one visible child, false means it don't has any visible child.
     *
     * @return False if setting view tree don't has any visible child.
     */
    public synchronized boolean hasVisibleChild() {
        if (ICameraSettingView.JUST_DISABLE_UI_WHEN_NOT_SELECTABLE) {
            return mSettingViewList.size() > 0;
        }

        boolean visible = false;
        for (ICameraSettingView view : mSettingViewList) {
            if (view.isEnabled()) {
                visible = true;
            }
        }
        return visible;
    }

    public int getSettingType(){
        return 0;
    }

    private class OnOrientationChangeListenerImpl implements IApp.OnOrientationChangeListener {

        @Override
        public void onOrientationChanged(int orientation) {
            if (mSettingView != null && mSettingView.getChildCount() != 0) {
                CameraUtil.rotateRotateLayoutChildView(mApp.getActivity(), mSettingView,
                        orientation, true);
            }
        }
    }
}
