package com.mediatek.camera.ui.prize;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.camera.R;
import com.mediatek.camera.common.app.IApp;

import java.util.List;

public class PrizeSettingSubView extends LinearLayout implements View.OnClickListener{

    private PrizeCameraSettingView mSettingView;
    private PrizeSubSettingCallback mCallback;

    interface PrizeSubSettingCallback{
        public void onSubSettingChanged();
    }

    public PrizeSettingSubView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCallback(PrizeSubSettingCallback callback){
        mCallback = callback;
    }

    public void setSettingView(IApp app, PrizeCameraSettingView view){
        mSettingView = view;

        removeAllViews();

        List<String> entryValues = mSettingView.getEntryValues();


        int index = 0;
        for(int i = 0; i < entryValues.size(); i++){
            if(entryValues.get(i).equals(mSettingView.getValue())){
                index = i;
                break;
            }
        }

        if(null != entryValues && entryValues.size() > 0){
            for(int i = 0; i < entryValues.size(); i++){
                View v = app.getActivity().getLayoutInflater().inflate(R.layout.prize_sub_setting_item,null);
                TextView textView = v.findViewById(R.id.grid_text_view);
                textView.setText(mSettingView.getEntrys().get(i));
                textView.setCompoundDrawablesWithIntrinsicBounds(null,
                        getContext().getResources().getDrawable(mSettingView.getIcons()[i]), null, null);
                v.setTag(textView);
                addView(v);
                v.setId(i);
                v.setOnClickListener(this);
                v.setSelected(index == i);

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)v.getLayoutParams();
                params.width = LayoutParams.MATCH_PARENT;
                params.weight = 1;
                v.setLayoutParams(params);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void onClick(View v) {
        int index = v.getId();
        if(index < mSettingView.getEntryValues().size()){
            for(int i = 0; i < getChildCount(); i++){
                View child = getChildAt(i);
                child.setSelected(i == index);
            }

            mSettingView.onValueChanged(mSettingView.getEntryValues().get(index));

            if(null != mCallback){
                mCallback.onSubSettingChanged();
            }
        }
    }
}
