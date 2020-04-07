package com.cydroid.note.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;

import java.util.ArrayList;

import cyee.changecolors.ChameleonColorManager;
import cyee.widget.CyeeTextView;

public class SearchLabelAdapt extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<LabelManager.LabelHolder> mLabels;
    private boolean mIsSecuritySpace;

    public SearchLabelAdapt(Activity activity) {
        mInflater = LayoutInflater.from(activity);
        mIsSecuritySpace = activity.getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
    }

    public void setLabels(ArrayList<LabelManager.LabelHolder> lables) {
        mLabels = lables;
    }

    @Override
    public int getCount() {
        return mLabels.size();
    }

    @Override
    public String getItem(int i) {
        return mLabels.get(i).mContent;
    }

    @Override
    public long getItemId(int i) {
        return mLabels.get(i).mId;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.search_label_item, null);
        }
        if (convertView instanceof CyeeTextView) {
            ((TextView) convertView).setText(getItem(i));
            if(ChameleonColorManager.isNeedChangeColor()) {
                ((TextView) convertView).setTextColor(ColorThemeHelper.
                        getContentNormalTextColor(NoteAppImpl.getContext(), mIsSecuritySpace));
            }
        }
        convertView.setTag((int) getItemId(i));
        return convertView;
    }
}
