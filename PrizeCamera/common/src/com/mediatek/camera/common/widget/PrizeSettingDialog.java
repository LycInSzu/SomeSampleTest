package com.mediatek.camera.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.DividerItemDecoration;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;
import com.mediatek.camera.common.debug.LogHelper;
import com.mediatek.camera.common.debug.LogUtil;
import com.mediatek.camera.common.preference.PrizeListPreference;

import java.util.List;

public class PrizeSettingDialog extends Dialog {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(PrizeListPreference.class.getSimpleName());

    private Context mContext;
    private CharSequence mTitle;
    private ClickListenerInterface mOnClickListener;
    private RecyclerView mList;
    /*prize-modify-add function item highlighted in the setting item is highlighted-xiaoping-20190409-start*/
    private String mSelectValue;
    /*prize-modify-add function item highlighted in the setting item is highlighted-xiaoping-20190409-end*/
    public PrizeSettingDialog(Context context, CharSequence title) {
        super(context);
        mContext = context;
        mTitle = title;
    }


    public PrizeSettingDialog(Context context, String title) {
        super(context/*, R.style.PrizeCustomDialog*/);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        initWindow();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.prize_setting_dialog_layout, null);
        setContentView(view);

        TextView tvTitle = (TextView) view.findViewById(R.id.title);
        TextView tvCancel = (TextView) view.findViewById(R.id.cancel);
        mList = (RecyclerView) view.findViewById(R.id.rv_dialog_list);
        mList.setLayoutManager(new LinearLayoutManager(mContext));
        //DividerItemDecoration divider = new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL);
        //mList.addItemDecoration(divider);
        tvTitle.setText(mTitle);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    private void initWindow() {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window dialogWindow = getWindow();
        View decorView = dialogWindow.getDecorView();
        if (decorView != null)
        decorView.setPadding(0, 0, 0, 0);
        dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        WindowManager.LayoutParams mParams = dialogWindow.getAttributes();
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int displayWidth = dm.widthPixels;
        int margin = (int)getContext().getResources().getDimension(R.dimen.dialog_margin);
        mParams.width = displayWidth - margin*2;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.gravity = Gravity.BOTTOM;
        dialogWindow.setAttributes(mParams);
        dialogWindow.setWindowAnimations(R.style.DialogBottomMenuAnimation);


    }

    public void initData(CharSequence[] entries){
        LogHelper.d(TAG, "[onClick] entries = " + entries);
        SettingDialogAdapter settingDialogAdapter = new SettingDialogAdapter(entries);
        mList.setAdapter(settingDialogAdapter);
    }


    public void setClicklistener(ClickListenerInterface clickListenerInterface) {
        mOnClickListener = clickListenerInterface;
    }

    public void initData(List<String> entryValues) {
        LogHelper.d(TAG, "[onClick] entryValues = " + entryValues);
        if (entryValues == null){
            return;
        }
        String[] entries = new String[entryValues.size()];
        for (int i = 0;i < entryValues.size(); i++){
            entries[i] = entryValues.get(i);
        }
        SettingDialogAdapter settingDialogAdapter = new SettingDialogAdapter(entries);
        mList.setAdapter(settingDialogAdapter);
    }

    private class OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.cancel:
                    if (mOnClickListener != null){
                        mOnClickListener.doCancel();
                    }
                    break;
            }
        }
    }

    public interface ClickListenerInterface {
        void doCancel();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }


    private class SettingDialogAdapter extends RecyclerView.Adapter<SettingDialogAdapter.DialogViewHolder>{

        private final CharSequence[] mEntries;
        private String[] data = {"111","222","333"};
        public SettingDialogAdapter(CharSequence[] entries) {
            this.mEntries = entries;
        }

        @Override
        public DialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View item = View.inflate(mContext,R.layout.prize_setting_dialog_item,null);
            DialogViewHolder dialogViewHolder = new DialogViewHolder(item);
            return dialogViewHolder;
        }

        @Override
        public void onBindViewHolder(DialogViewHolder holder, int position) {
            holder.mTvTitle.setText(mEntries[position]);
            /*prize-modify-add function item highlighted in the setting item is highlighted-xiaoping-20190409-start*/
            if (mEntries[position].equals(mSelectValue)) {
                holder.mTvTitle.setSelected(true);
            } else {
                holder.mTvTitle.setSelected(false);
            }
            /*prize-modify-add function item highlighted in the setting item is highlighted-xiaoping-20190409-end*/
            if (position == mEntries.length-1){
                holder.mDivider.setVisibility(View.INVISIBLE);
                holder.mItemContainer.setBackground(mContext.getDrawable(R.drawable.prize_selector_popup_window_under_btn_bg));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null){
                        mOnItemClickListener.onItemClick(position);
                        PrizeSettingDialog.this.dismiss();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mEntries.length;
        }

        public class DialogViewHolder extends RecyclerView.ViewHolder{

            public TextView mTvTitle;
            public View mDivider;
            public View mItemContainer;

            public DialogViewHolder(View itemView) {
                super(itemView);
                mTvTitle = itemView.findViewById(R.id.title);
                mDivider = itemView.findViewById(R.id.divider);
                mItemContainer = itemView.findViewById(R.id.item_container);
            }
        }
    }

    /*prize-modify-add function item highlighted in the setting item is highlighted-xiaoping-20190409-start*/
    public void setSelectValue(String value) {
        mSelectValue = value;
    }
    /*prize-modify-add function item highlighted in the setting item is highlighted-xiaoping-20190409-end*/
}
