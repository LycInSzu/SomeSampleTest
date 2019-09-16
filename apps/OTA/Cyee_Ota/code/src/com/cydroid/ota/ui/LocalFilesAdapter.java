package com.cydroid.ota.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cydroid.ota.SettingUpdateApplication;
import com.cydroid.ota.SystemTheme;
import com.cydroid.ota.R;
import com.cydroid.ota.bean.LocalFileInfo;

import java.util.List;

/**
 * Created by liuyanfeng on 15-6-9.
 */
public class LocalFilesAdapter extends BaseAdapter {
    private List<LocalFileInfo> mLocalFileVoList;
    private Context mContext;
    private SystemTheme mSystemTheme;

    public LocalFilesAdapter(Context context, List<LocalFileInfo> fileVos) {
        mContext = context;
        mLocalFileVoList = fileVos;
        SettingUpdateApplication application = (SettingUpdateApplication) context.getApplicationContext();
        mSystemTheme = application.getSystemTheme();
    }

    public void updateDatas(List<LocalFileInfo> fileVos) {
        mLocalFileVoList = fileVos;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mLocalFileVoList.size();
    }

    @Override
    public LocalFileInfo getItem(int i) {
        return mLocalFileVoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LocalFileInfo fileVo = mLocalFileVoList.get(i);
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.gn_su_layout_local_file_item, null);
            holder.mIconView = (ImageView) view
                    .findViewById(R.id.gn_su_id_local_fileIcon);
            holder.mFileName = (TextView) view
                    .findViewById(R.id.gn_su_id_local_fileName);
            holder.mFileSize = (TextView) view
                    .findViewById(R.id.gn_su_id_local_fileSize);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (fileVo.isFolder()) {
            holder.mIconView
                    .setImageResource(R.drawable.gn_su_folder_icon_light);
        } else if (fileVo.getFileName().endsWith(".zip")) {
            holder.mIconView
            .setImageResource(R.drawable.gn_su_file_icon_zip_light);
        }else {
            holder.mIconView
                    .setImageResource(R.drawable.file_icon_default_light);
        }
        holder.mFileName.setText(fileVo.getFileName());
        holder.mFileSize.setText(fileVo.getFileSize());
        if (mSystemTheme.isNeedChangeColor()) {
            holder.mFileName.setTextColor(mSystemTheme.getChameleon().ContentColorPrimaryOnBackgroud_C1);
            holder.mFileSize.setTextColor(mSystemTheme.getChameleon().ContentColorPrimaryOnBackgroud_C1);
        }
        return view;
    }

    private static class ViewHolder {
        public ImageView mIconView;
        public TextView mFileName;
        public TextView mFileSize;
    }
}
