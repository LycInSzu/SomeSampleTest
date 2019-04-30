package com.mediatek.camera.ui.prize;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.mode.professional.ProfessionalModeEntry;
import com.mediatek.camera.feature.mode.slowmotion.SlowMotionEntry;
import com.mediatek.camera.prize.FeatureSwitcher;
import com.mediatek.camera.prize.PrizePluginModeManager;
import com.prize.camera.feature.mode.gif.GifModeEntry;
import com.prize.camera.feature.mode.filter.FilterModeEntry;
import com.prize.camera.feature.mode.pano.PanoModeEntry;
import com.prize.camera.feature.mode.smartscan.SmartScanModeEntry;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by huangpengfei on 2019/3/5.
 */
public class PrizePluginModeFragment extends Fragment {

    private RecyclerView mModeListView;
    private ArrayList<Item> mModeItemMap = new ArrayList();
    private LogUtil.Tag TAG = new LogUtil.Tag(PrizePluginModeFragment.class.getSimpleName());
    private Context mContext;
    private String mCurPluginMode;

    private int mCameraId;

    public void setCameraId(int cameraid){
        mCameraId = cameraid;
        initItemData();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        initItemData();
        if (mStateListener != null) {
            mStateListener.onCreate();
        }
    }

    private void initItemData() {

        if(null == mContext){
            return;
        }

        mModeItemMap.clear();

        mModeItemMap.add(new Item(mContext.getResources().getString(R.string.shutter_type_photo_gif), mContext.getDrawable(R.drawable.prize_gif_selector), GifModeEntry.class.getName()));
        mModeItemMap.add(new Item(mContext.getResources().getString(R.string.shutter_type_photo_filter), mContext.getDrawable(R.drawable.prize_filter_selector), FilterModeEntry.class.getName()));

        if(mCameraId == 0){
            mModeItemMap.add(new Item(mContext.getResources().getString(R.string.shutter_type_photo_panorama), mContext.getDrawable(R.drawable.prize_pano_selector), PanoModeEntry.class.getName()));
            if(FeatureSwitcher.isSupportSlowMotion()){
                mModeItemMap.add(new Item(mContext.getResources().getString(R.string.shutter_type_photo_slow_motion), mContext.getDrawable(R.drawable.prize_slow_motion_selector), SlowMotionEntry.class.getName()));
            }
            mModeItemMap.add(new Item(mContext.getResources().getString(R.string.shutter_type_photo_intelligent_scanning), mContext.getDrawable(R.drawable.prize_scanner_selector), SmartScanModeEntry.class.getName()));
            mModeItemMap.add(new Item(mContext.getResources().getString(R.string.pref_camera_professional_title), mContext.getDrawable(R.drawable.prize_professional_selector), ProfessionalModeEntry.class.getName()));
        }

        mCurPluginMode = PrizePluginModeManager.getPluginMode(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.prize_plugin_mode_fragment, null, false);
        mModeListView = (RecyclerView) rootView.findViewById(R.id.plugin_mode_list);
        View back = rootView.findViewById(R.id.plugin_mode_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBackClickListener != null) {
                    mOnBackClickListener.onClick();
                }
            }
        });
        GridLayoutManager manager = new GridLayoutManager(mContext, 3);
        mModeListView.setLayoutManager(manager);
        PluginModeItemAdapter pluginModeItemAdapter = new PluginModeItemAdapter(mContext);
        mModeListView.setAdapter(pluginModeItemAdapter);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStateListener != null) {
            mStateListener.onDestroy();
        }
    }

    private StateListener mStateListener;

    public interface StateListener {
        void onCreate();

        void onDestroy();
    }

    public void setStateListener(StateListener listener) {
        mStateListener = listener;
    }

    private OnBackClickListener mOnBackClickListener;

    public interface OnBackClickListener {
        void onClick();
    }

    public void setOnBackClickListener(OnBackClickListener onBackClickListener) {
        mOnBackClickListener = onBackClickListener;
    }

    private OnPluginModeItemClickListener mOnPluginModeItemClickListener;

    public interface OnPluginModeItemClickListener {
        void onItemClick(String mode);
    }

    public void setOnPluginModeItemClickListener(OnPluginModeItemClickListener onPluginModeItemClickListener) {
        mOnPluginModeItemClickListener = onPluginModeItemClickListener;
    }

    class PluginModeItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LayoutInflater mLayoutInflater;

        public PluginModeItemAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ModeViewHolder modeViewHolder = new ModeViewHolder(mLayoutInflater.inflate(R.layout.prize_plugin_mode_item, null, false));
            return modeViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ModeViewHolder) {
                ((ModeViewHolder) holder).mTextView.setText(mModeItemMap.get(position).getmTitle());
                ((ModeViewHolder) holder).mImageView.setImageDrawable(mModeItemMap.get(position).getmIcon());
                if(mModeItemMap.get(position).getModeName().equals(mCurPluginMode)){
                    ((ModeViewHolder) holder).setSelected(true);
                }else{
                    ((ModeViewHolder) holder).setSelected(false);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mModeItemMap.size();
        }

        private class ModeViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {
            TextView mTextView;
            ImageView mImageView;
            View mModeView;

            ModeViewHolder(View view) {
                super(view);
                mModeView = view;
                mTextView = (TextView) view.findViewById(R.id.tv_title);
                mImageView = (ImageView) view.findViewById(R.id.iv_icon);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int adapterPosition = getAdapterPosition();
                if (mOnPluginModeItemClickListener != null) {
                    if(adapterPosition < mModeItemMap.size()){
                        mOnPluginModeItemClickListener.onItemClick(mModeItemMap.get(adapterPosition).getModeName());
                    }
                }
                LogHelper.d(TAG, "[onClick] adapterPosition = " + adapterPosition + "  title = " + mTextView.getText());
            }

            public void setSelected(boolean selected){
                mImageView.setSelected(selected);
            }
        }
    }

    class Item {
        private String mTitle;
        private Drawable mIcon;
        private String mModeName;

        public Item(String mTitle, Drawable mIcon, String mode) {
            this.mTitle = mTitle;
            this.mIcon = mIcon;
            this.mModeName = mode;
        }

        public String getmTitle() {
            return mTitle;
        }

        public void setmTitle(String mTitle) {
            this.mTitle = mTitle;
        }

        public Drawable getmIcon() {
            return mIcon;
        }

        public void setmIcon(Drawable mIcon) {
            this.mIcon = mIcon;
        }

        public String getModeName(){
            return mModeName;
        }
    }
}
