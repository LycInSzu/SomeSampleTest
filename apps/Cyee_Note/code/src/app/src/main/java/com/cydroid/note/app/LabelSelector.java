package com.cydroid.note.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import cyee.app.CyeeAlertDialog;

import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.common.Constants;
import com.cydroid.note.encrypt.EncryptDetailActivity;

import java.util.ArrayList;

public class LabelSelector implements View.OnClickListener {
    private static final String TAG = "LabelSelector";
    private Activity mActivity;
    private LayoutInflater mInflater;
    private Dialog mDialog;
    private ArrayList<Integer> mSelectLabelIds = new ArrayList<>();
    private OnLabelChangedListener mLabelChangedListener;
    private IYouJuCallback mYouJuCb;
    private ArrayList<LabelManager.LabelHolder> mLabels;
    private LabelSelectorAdapter mLabelAdapter;


    public interface OnLabelChangedListener {
        void onLabelChanged(ArrayList<Integer> ids);

        void onUpdate();
    }


    public LabelSelector(Activity activity, OnLabelChangedListener listener) {
        mActivity = activity;
        mLabelChangedListener = listener;
        mInflater = LayoutInflater.from(activity);
    }

    public void setYouJuCb(IYouJuCallback cb) {
        mYouJuCb = cb;
    }

    public void updateLabelList(ArrayList<Integer> ids) {
        if (mLabels.size() == 0) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            return;
        }

        if (mDialog != null && mDialog.isShowing()) {
            notifyDataSetChanged();
        } else {
            selectLabel(ids);
        }
    }

    public void setLabels(ArrayList<LabelManager.LabelHolder> labels) {
        mLabels = labels;
        if (labels.size() == 0) {
            mSelectLabelIds.clear();
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            return;
        }

        removeInvalidLabel();
        notifyDataSetChanged();
    }

    public void selectLabel(ArrayList<Integer> ids) {
        mSelectLabelIds.clear();
        mSelectLabelIds.addAll(ids);

        if (mLabels.size() == 0) {
            mSelectLabelIds.clear();
            enterCustomLabel();
            return;
        }
        removeInvalidLabel();

        if (mDialog == null) {
            mDialog = createDialog();
        }

        if (!mDialog.isShowing()) {
            mDialog.show();
        }
        mLabelAdapter.notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        if (mLabelAdapter != null) {
            mLabelAdapter.notifyDataSetChanged();
        }
    }

    private void removeInvalidLabel() {
        ArrayList<Integer> invalid = new ArrayList<>();
        ArrayList<LabelManager.LabelHolder> labels = mLabels;
        ArrayList<Integer> labelIds = mSelectLabelIds;
        for (Integer id : labelIds) {
            if (!isValid(id, labels)) {
                invalid.add(id);
            }
        }

        labelIds.removeAll(invalid);
    }

    private boolean isValid(int id, ArrayList<LabelManager.LabelHolder> labels) {
        for (LabelManager.LabelHolder labelHolder : labels) {
            if (labelHolder.mId == id) {
                return true;
            }
        }
        return false;
    }

    private Dialog createDialog() {
        View content = LayoutInflater.from(mActivity).inflate(R.layout.label_selector_layout, null, false);
        ListView gridView = (ListView) content.findViewById(R.id.label_select_list);
        LabelSelectorAdapter labelAdapter = new LabelSelectorAdapter();
        mLabelAdapter = labelAdapter;
        gridView.setAdapter(labelAdapter);

        CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(mActivity);
        builder.setView(content);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mLabelChangedListener != null) {
                    mLabelChangedListener.onLabelChanged(mSelectLabelIds);
                }
                ArrayList<Integer> labelIds = mSelectLabelIds;
                for (Integer id : labelIds) {
                    String labelName = getLabelContentById(id);
                    if (labelName != null) {
                    }
                }
                dismissDialog();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissDialog();
            }
        });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                mSelectLabelIds.clear();
                if (mLabelChangedListener != null) {
                    mLabelChangedListener.onUpdate();
                }
            }
        });
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setGravity(Gravity.BOTTOM);
        CheckBox checkBox = (CheckBox) content.findViewById(R.id.label_select_custom_label);
        Drawable icon = ContextCompat.getDrawable(mActivity, R.drawable.label_selector_custom_label);
        Drawable tintIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTintList(tintIcon, ContextCompat.
                getColorStateList(mActivity, R.color.action_bar_image_color));
        checkBox.setButtonDrawable(tintIcon);
        checkBox.setOnClickListener(this);
        return dialog;
    }

    private void enterCustomLabel() {
        try {
            Intent intent = new Intent(mActivity, LabelCustomActivity.class);
            if (mActivity instanceof EncryptDetailActivity) {
                intent.putExtra(Constants.IS_SECURITY_SPACE,
                        mActivity.getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));
            }
            mActivity.startActivityForResult(intent, NewNoteActivity.REQUEST_CUSTOM_LABEL);
        } catch (Exception e) {
            Log.d(TAG, "enterCustomLabel fail : " + e.toString());
        }
    }

    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void cancel() {
        dismissDialog();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.label_select_custom_label: {
                enterCustomLabel();
                break;
            }
            default: {
                break;
            }
        }
    }

    private boolean isSelected(int labelId) {
        return mSelectLabelIds.contains(labelId);
    }

    public String getLabelContentById(int id) {
        ArrayList<LabelManager.LabelHolder> labels = mLabels;
        for (LabelManager.LabelHolder holder : labels) {
            if (holder.mId == id) {
                return holder.mContent;
            }
        }
        return null;
    }

    public boolean isLabelInvalid(ArrayList<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            return true;
        }
        ArrayList<LabelManager.LabelHolder> labels = mLabels;
        for (LabelManager.LabelHolder holder : labels) {
            if (ids.contains(holder.mId)) {
                return false;
            }
        }
        return true;
    }


    private class LabelSelectorAdapter extends BaseAdapter {
        private CheckListener mCheckListener = new CheckListener();

        public LabelSelectorAdapter() {
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
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.label_selector_item, null);
                viewHolder = new ViewHolder();
                viewHolder.contentView = convertView;
                viewHolder.textView = (TextView) convertView.findViewById(R.id.label_select_list_item_text);
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.label_select_list_item_checkbox);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.textView.setText(getItem(i));
            viewHolder.contentView.setOnClickListener(mCheckListener);
            int labelId = (int) getItemId(i);
            viewHolder.labelId = labelId;
            boolean checked = isSelected(labelId) ? true : false;
            viewHolder.checkBox.setChecked(checked);

            return convertView;
        }

        private class CheckListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                ViewHolder viewHolder = (ViewHolder) v.getTag();
                int id = viewHolder.labelId;
                boolean checked = viewHolder.checkBox.isChecked();
                if (checked) {
                    mSelectLabelIds.remove(Integer.valueOf(id));
                } else {
                    mSelectLabelIds.add(id);
                }
                viewHolder.checkBox.toggle();
            }
        }
    }

    private static class ViewHolder {
        public View contentView;
        public TextView textView;
        public CheckBox checkBox;
        public int labelId;
    }
}
