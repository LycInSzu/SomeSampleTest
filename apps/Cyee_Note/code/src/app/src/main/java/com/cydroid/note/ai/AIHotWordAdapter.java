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

/**
 * Created by wuguangjie on 16-3-28.
 */
public class AIHotWordAdapter extends BaseAdapter {
    private List<HotWordUtils.HotWord> mHotWords;
    private LayoutInflater mInflater;
    private boolean mIsSecuritySpace;

    public AIHotWordAdapter(List<HotWordUtils.HotWord> hotWords, LayoutInflater inflater,
                            boolean isSecuritySpace) {
        this.mHotWords = hotWords;
        this.mInflater = inflater;
        mIsSecuritySpace = isSecuritySpace;
    }

    @Override
    public int getCount() {
        if (mHotWords != null) {
            return mHotWords.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mHotWords != null) {
            return mHotWords.get(position);
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
        holder.textView.setTextColor(ColorThemeHelper.getContentNormalTextColor(NoteAppImpl.getContext(), mIsSecuritySpace));
        holder.textView.setText(mHotWords.get(position).getTitle());
        return convertView;
    }

    private static class ViewHolder {
        TextView textView;
    }
}
