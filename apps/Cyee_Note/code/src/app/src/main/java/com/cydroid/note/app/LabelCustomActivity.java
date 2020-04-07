package com.cydroid.note.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cydroid.note.common.Log;
import com.cydroid.note.R;
import com.cydroid.note.app.dialog.CyeeConfirmDialog;
import com.cydroid.note.app.view.NoteLabelAddView;
import com.cydroid.note.app.view.StandardActivity;

import java.util.ArrayList;

public class LabelCustomActivity extends StandardActivity implements StandardActivity.StandardAListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "LabelCustomActivity";
    private static final int MESSAGE_ADD_LABEL = 0;
    private static final int MESSAGE_REMOVE_LABEL = 1;
    private static final int MESSAGE_UPDATE_LABEL = 2;
    private NoteLabelAddView mNoteLabelAddView;
    private ListView mListView;
    private LayoutInflater mInflater;
    private LabelCustomAdapter mAdapter;
    private Handler mWorkHandler;
    private Handler mMainHandler;
    private HandlerThread mWorkThread;
    private ArrayList<LabelManager.LabelHolder> mLabels;
    private LabelManager.LabelDataChangeListener mLabelDataChangeListener;
    private LabelManager mLabelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.label_custom_action_bar_title);
        setStandardAListener(this);
        setNoteContentView(R.layout.label_custom_content_layout);
        setNoteRootViewBackgroundColor();

        initHandler();
        initData();
        initView();
        setResult();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLabelManager.removeLabelDataChangeListener(mLabelDataChangeListener);
        mWorkHandler.removeCallbacksAndMessages(null);
        mWorkHandler = null;
        mWorkThread.quit();
        mWorkThread = null;
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
    }

    private void initData() {
        NoteAppImpl app = (NoteAppImpl) getApplication();
        LabelManager labelManager = app.getLabelManager();
        mLabelDataChangeListener = new LabelManager.LabelDataChangeListener() {
            @Override
            public void onDataChange() {
                if (DEBUG) {
                    Log.d(TAG, "onDataChange");
                }
                if (mMainHandler != null) {
                    mMainHandler.sendEmptyMessage(MESSAGE_UPDATE_LABEL);
                }
            }
        };
        labelManager.addLabelDataChangeListener(mLabelDataChangeListener);
        mLabelManager = labelManager;
        mLabels = labelManager.getLabelList();
        mInflater = LayoutInflater.from(getApplicationContext());
        mAdapter = new LabelCustomAdapter();
    }

    private void initHandler() {
        HandlerThread workThread = new HandlerThread("label_custom_work_thread");
        workThread.start();
        mWorkThread = workThread;
        mWorkHandler = new Handler(workThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_ADD_LABEL: {
                        String text = (String) msg.obj;
                        mLabelManager.addLabel(text);
                        break;
                    }
                    case MESSAGE_REMOVE_LABEL: {
                        int id = (Integer) msg.obj;
                        mLabelManager.removeLabelById(id);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        };
        mMainHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_UPDATE_LABEL: {
                        mLabels = mLabelManager.getLabelList();
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        };
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.label_custom_list);
        mListView.setAdapter(mAdapter);
        mNoteLabelAddView = (NoteLabelAddView) findViewById(R.id.note_label_add_view);
        mNoteLabelAddView.setOnAddLabelListener(new NoteLabelAddView.OnAddLabelListener() {
            @Override
            public void onAddLabel(String newLabelName) {
                saveCustomLabel(newLabelName);
            }
        });
    }

    private void setResult() {
        setResult(RESULT_OK, null);
    }

    private void saveCustomLabel(String labelName) {
        if (containLabel(labelName)) {
            Toast.makeText(this, R.string.label_custom_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mWorkHandler != null) {
            mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MESSAGE_ADD_LABEL, labelName));
        }
    }

    private boolean containLabel(String content) {
        ArrayList<LabelManager.LabelHolder> labels = mLabels;
        for (LabelManager.LabelHolder holder : labels) {
            if (TextUtils.equals(holder.mContent, content)) {
                return true;
            }
        }
        return false;
    }

    private void deleteLabel(View view) {
        final int id = (Integer) view.getTag();
        CyeeConfirmDialog dialog = new CyeeConfirmDialog(this);
        dialog.setTitle(R.string.button_delete);
        dialog.setMessage(R.string.label_custom_delete_attention);
        dialog.setOnClickListener(new CyeeConfirmDialog.OnClickListener() {
            @Override
            public void onClick(int which) {
                if (which == CyeeConfirmDialog.BUTTON_POSITIVE) {
                    if (mWorkHandler != null) {
                        mWorkHandler.sendMessage(mWorkHandler.obtainMessage(MESSAGE_REMOVE_LABEL, id));
                    }
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onClickHomeBack() {
        mNoteLabelAddView.clearFocus();
        finish();
    }

    @Override
    public void onClickRightView() {

    }


    private class LabelCustomAdapter extends BaseAdapter {

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
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.label_custom_item, null, false);
                holder.textView = (TextView) convertView.findViewById(R.id.label_custom_list_item_text);
                holder.deleteButton = (ImageView) convertView.findViewById(R.id.label_custom_list_item_delete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.textView.setText(getItem(i));
            holder.deleteButton.setTag((int) getItemId(i));
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteLabel(view);
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        TextView textView;
        ImageView deleteButton;
    }
}
