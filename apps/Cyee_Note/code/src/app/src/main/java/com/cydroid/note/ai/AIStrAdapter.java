package com.cydroid.note.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.NoteAppImpl;
import com.cydroid.note.common.ColorThemeHelper;

import java.util.List;

import cyee.changecolors.ChameleonColorManager;

/**
 * Created by wuguangjie on 16-3-28.
 */
public class AIStrAdapter extends BaseAdapter {
    private List<String> mKeyWords;
    private LayoutInflater mInflater;
    private boolean mIsSecuritySpace;

    public AIStrAdapter(List<String> hotWords, LayoutInflater inflater, boolean isSecuritySpace) {
        this.mKeyWords = hotWords;
        this.mInflater = inflater;
        mIsSecuritySpace = isSecuritySpace;
    }

    @Override
    public int getCount() {
        if (mKeyWords != null) {
            return mKeyWords.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mKeyWords != null) {
            return mKeyWords.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.recommand_grid_view_item_ly, null);
            holder.textView = (TextView) convertView.findViewById(R.id.main_grid_item_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (ChameleonColorManager.isNeedChangeColor()) {
            holder.textView.setTextColor(ColorThemeHelper.getContentNormalTextColor(NoteAppImpl.getContext(),
                    mIsSecuritySpace));
        }
        holder.textView.setText(mKeyWords.get(position));
        return convertView;
    }

    private static class ViewHolder {
        TextView textView;
    }
}
