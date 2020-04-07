package com.cydroid.note.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.cydroid.note.R;
import com.cydroid.note.app.view.NoteGridLayoutManager;
import com.cydroid.note.app.view.StandardActivity;
import com.cydroid.note.common.ColorThemeHelper;
import com.cydroid.note.common.Constants;
import com.cydroid.note.common.NoteUtils;
import com.cydroid.note.data.DataManager;
import com.cydroid.note.data.LabelNoteSet;
import com.cydroid.note.data.LabelSource;
import com.cydroid.note.data.Path;
import com.cydroid.note.encrypt.EncryptDetailActivity;

public class LabelNoteActivity extends StandardActivity implements LoadingListener,
        RecyclerViewBaseAdapter.OnTouchListener, StandardActivity.StandardAListener {
    public static final String SELECT_LABEL_ID = "label_id";
    public static final String SELECT_LABEL_NAME = "label_name";
    public static final String SOURCE_PATH = "source_path";
    public static final String SECRET = "secret";
    public static final String LOCAL = "local";
    private String mLabelName;
    private LabelNoteSet mNoteSet;
    private RecyclerViewBaseAdapter mAdapter;
    private View mTipView;
    private TextView mTipTextView;
    private RecyclerView mRecyclerView;
    private LabelManager.LabelDataChangeListener mLabelDataChangeListener;
    private LabelManager mLabelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLabelName = getIntent().getStringExtra(SELECT_LABEL_NAME);
        setTitle(mLabelName);
        setStandardAListener(this);

        setNoteContentView(R.layout.label_note_activity_content_layout);
        setNoteRootViewBackgroundColor();
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
        mLabelManager.removeLabelDataChangeListener(mLabelDataChangeListener);
    }

    private void initData() {
        NoteAppImpl app = (NoteAppImpl) getApplication();
        DataManager dataManager = app.getDataManager();
        int labelId = getIntent().getIntExtra(SELECT_LABEL_ID, -1);
        String sourcePathString = getIntent().getStringExtra(SOURCE_PATH);
        String setPath = LabelSource.LABEL_SET_PATH;
        if (SECRET.equals(sourcePathString)) {
            setPath = LabelSource.SECRET_LABEL_SET_PATH;
        }
        mNoteSet = (LabelNoteSet) dataManager.getMediaSet(setPath);
        mNoteSet.setLabel(labelId);

        LabelManager labelManager = app.getLabelManager();
        mLabelManager = labelManager;
        mLabelDataChangeListener = new LabelManager.LabelDataChangeListener() {
            @Override
            public void onDataChange() {
                mNoteSet.setLabels(mLabelManager.getLabelList());
            }
        };
        labelManager.addLabelDataChangeListener(mLabelDataChangeListener);
        mNoteSet.setLabels(labelManager.getLabelList());
    }

    private void initView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.label_recycler_view);
        mRecyclerView = recyclerView;
        mRecyclerView.addItemDecoration(new NoteItemDecoration(NoteAppImpl.getContext()));

        int displayMode = NoteUtils.getDisplayMode();
        if (displayMode == Constants.NOTE_DISPLAY_LIST_MODE) {
            mAdapter = new ListRecyclerViewAdapter(this, mNoteSet, this, null, false);
        } else {
            mAdapter = new GrideRecyclerViewAdapter(this, mNoteSet, this, null, false);
        }
        mAdapter.setTimeTextColor(ContextCompat.getColor(this, R.color.home_note_item_time_color));
        mAdapter.setOnTouchListener(this);
        int column = displayMode == Constants.NOTE_DISPLAY_LIST_MODE ? 1 : 2;
        mRecyclerView.setLayoutManager(new NoteGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        mTipView = findViewById(R.id.note_tip_view);
        mTipTextView = (TextView) findViewById(R.id.note_tip_text_view);
        boolean isSecuritySpace = getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false);
        int color = ColorThemeHelper.isDarkBgColor(this, isSecuritySpace) ?
                ContextCompat.getColor(this, R.color.note_tip_text_dart_bg_color) :
                ContextCompat.getColor(this, R.color.note_tip_text_white_bg_color);
        mTipTextView.setTextColor(color);
    }

    @Override
    public void onLoadingStarted() {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mTipView.setVisibility(View.VISIBLE);
            mTipTextView.setText(R.string.note_tip_search);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTipView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadingFinished(boolean loadingFailed) {
        int count = mAdapter.getItemCount();
        if (count == 0) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mTipView.setVisibility(View.VISIBLE);
            mTipTextView.setText(R.string.note_tip_searchFinish);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mTipView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSingleClickTouch(Path path) {
        try {
            Intent intent = new Intent();
            boolean isEncrypt = getIntent().getBooleanExtra(Constants.NOTE_IS_CRYPTED, false);
            if (isEncrypt) {
                intent.setClass(LabelNoteActivity.this, EncryptDetailActivity.class);
                intent.putExtra(EncryptDetailActivity.NOTE_ITEM_PATH, path.toString());
                intent.putExtra(Constants.IS_SECURITY_SPACE,
                        getIntent().getBooleanExtra(Constants.IS_SECURITY_SPACE, false));
            } else {
                intent.setClass(LabelNoteActivity.this, NewNoteActivity.class);
                intent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path.toString());
                intent.putExtra(NewNoteActivity.FROM_INNER_CONTEXT, true);
            }
            startActivity(intent);
            //Chenyee wanghaiyan 2018-1-5 modify for CSW1705A-1532 begin
        } catch (Exception e) {
            //Chenyee wanghaiyan 2018-1-5 modify for CSW1705A-1532 end
        }
    }

    @Override
    public void onLongClickTouch(Path path) {

    }

    @Override
    public void onClickHomeBack() {
        finish();
    }

    @Override
    public void onClickRightView() {

    }
}
